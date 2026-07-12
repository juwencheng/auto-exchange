package io.github.juwencheng.autoexchange.core.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.juwencheng.autoexchange.core.translate.cache.TranslateCacheManager;
import io.github.juwencheng.autoexchange.exception.ExchangeProcessingException;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用翻译策略。负责对象图遍历和调度翻译器执行。
 * <p>
 * 遍历逻辑只有一套，不同类型的翻译（汇率、字典等）通过 {@link FieldTranslator} 插件实现。
 * 翻译结果写入 {@link TranslateContext}，由序列化器统一追加到 JSON 输出。
 *
 * @author juwencheng
 */
public class TranslateStrategy {

    private static final Logger log = LoggerFactory.getLogger(TranslateStrategy.class);

    private static final Map<Class<?>, TranslateClassMetadata> CLASS_METADATA_CACHE = new ConcurrentHashMap<>(256);

    private final Map<Class<? extends FieldTranslator>, FieldTranslator> translatorInstances;
    private final TranslateCacheManager cacheManager;

    public TranslateStrategy(List<FieldTranslator> translators, TranslateCacheManager cacheManager) {
        this.translatorInstances = new HashMap<>();
        for (FieldTranslator translator : translators) {
            this.translatorInstances.put(translator.getClass(), translator);
        }
        this.cacheManager = cacheManager;
    }

    /**
     * 对对象图执行翻译处理
     *
     * @param rootObject 根对象
     */
    public void applyTranslation(Object rootObject) {
        if (rootObject == null) {
            return;
        }
        traverseObjectGraph(rootObject, new IdentityHashMap<>());
    }

    private void traverseObjectGraph(Object object, Map<Object, Object> visitedMap) {
        if (object == null || visitedMap.containsKey(object)) {
            return;
        }
        visitedMap.put(object, Boolean.TRUE);

        TranslateClassMetadata metadata = getClassMetadata(object.getClass());
        TranslateContext context = TranslateContextHolder.getContext();

        if (context == null) {
            return;
        }

        // 处理标注了 @TranslateField 的字段
        for (TranslateFieldMeta fieldMeta : metadata.getTranslateFields()) {
            Field field = fieldMeta.getField();
            boolean canAccess = field.canAccess(object);
            try {
                if (!canAccess) {
                    field.setAccessible(true);
                }
                Object fieldValue = field.get(object);

                FieldTranslator translator = translatorInstances.get(fieldMeta.getTranslatorClass());
                if (translator == null) {
                    log.warn("未找到翻译器实例: {}", fieldMeta.getTranslatorClass().getName());
                    continue;
                }

                // 创建包含字段参数的子上下文
                TranslateContext fieldContext = new TranslateContext();
                fieldContext.getAttributes().putAll(context.getAttributes());
                fieldContext.setAttribute("_fieldArgs", fieldMeta.getArgs());
                fieldContext.setAttribute("_sourceObject", object);
                fieldContext.setAttribute("_field", field);

                Object result = cacheManager.translateWithCache(
                        translator,
                        fieldMeta.getTranslatorClass(),
                        fieldMeta.getCacheStrategyClass(),
                        fieldValue,
                        fieldContext,
                        fieldMeta.getArgs(),
                        field);
                if (result != null) {
                    context.addAppendedData(object, fieldMeta.getOutputFieldName(), result);
                }
            } catch (IllegalAccessException e) {
                log.error("翻译字段失败: {}.{}", object.getClass().getName(), field.getName(), e);
                throw new ExchangeProcessingException(
                        "翻译字段失败: " + object.getClass().getName() + "." + field.getName(), e);
            } finally {
                if (!canAccess) {
                    field.setAccessible(false);
                }
            }
        }

        // 递归遍历子节点
        if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> traverseObjectGraph(item, visitedMap));
        } else if (object instanceof Map) {
            ((Map<?, ?>) object).values().forEach(item -> traverseObjectGraph(item, visitedMap));
        } else {
            for (PropertyDescriptor pd : metadata.getPropertiesToInspect()) {
                try {
                    Object propertyValue = pd.getReadMethod().invoke(object);
                    traverseObjectGraph(propertyValue, visitedMap);
                } catch (Exception e) {
                    throw new ExchangeProcessingException("遍历对象属性图失败", e);
                }
            }
        }
    }

    private TranslateClassMetadata getClassMetadata(Class<?> clazz) {
        return CLASS_METADATA_CACHE.computeIfAbsent(clazz, this::buildClassMetadata);
    }

    private TranslateClassMetadata buildClassMetadata(Class<?> clazz) {
        if (clazz.isPrimitive() || shouldSkip(clazz) || clazz.isArray()) {
            return new TranslateClassMetadata(Collections.emptyList(), Collections.emptyList());
        }

        List<PropertyDescriptor> propertiesToInspect = new ArrayList<>();
        List<TranslateFieldMeta> translateFields = new ArrayList<>();

        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                Method readMethod = pd.getReadMethod();
                if (readMethod != null && !isLeafType(readMethod.getReturnType())) {
                    propertiesToInspect.add(pd);
                }

                Field field = findField(clazz, pd.getName());
                if (field != null && field.isAnnotationPresent(TranslateField.class)) {
                    TranslateField annotation = field.getAnnotation(TranslateField.class);
                    String outputName = annotation.value();
                    if (outputName == null || outputName.trim().isEmpty()) {
                        outputName = field.getName() + "Translated";
                    }
                    translateFields.add(new TranslateFieldMeta(
                            field, outputName, annotation.translator(), annotation.args(),
                            annotation.cacheStrategy()));
                }
            }
        } catch (Exception e) {
            log.error("构建翻译类元数据失败: {}", clazz.getName(), e);
            throw new ExchangeProcessingException("构建翻译类元数据失败: " + clazz.getName());
        }

        return new TranslateClassMetadata(propertiesToInspect, translateFields);
    }

    private Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            return null;
        }
    }

    private boolean isLeafType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.getName().startsWith("java.lang")
                || clazz.getName().startsWith("java.math");
    }

    private boolean shouldSkip(Class<?> clazz) {
        String className = clazz.getName();
        return className.startsWith("java.") || className.startsWith("javax.");
    }
}
