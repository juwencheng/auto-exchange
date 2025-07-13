package tech.baizi.autoexchange.core.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.IApplyExchange;
import tech.baizi.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;
import tech.baizi.autoexchange.core.strategy.meta.ClassMetadata;
import tech.baizi.autoexchange.exception.AutoExchangeException;
import tech.baizi.autoexchange.exception.ExchangeProcessingException;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的应用汇率策略，提供一些模版方法
 */
public abstract class AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    private static final Logger log = LoggerFactory.getLogger(AbstractApplyExchangeStrategy.class);
    protected static final Map<Class<?>, ClassMetadata> CLASS_METADATA_CACHE = new ConcurrentHashMap<>(256);
    protected final AutoExchangeProperties properties;

    protected AbstractApplyExchangeStrategy(AutoExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object applyExchange(Object rootObject) {
        if (rootObject == null) {
            return rootObject;
        }
        beforeApplyExchange();
        Object o = processRecursively(rootObject, new IdentityHashMap<>());
        afterApplyExchange();
        return o;
    }

    /**
     * 循环处理对象，从底至上的方式处理每一个对象及其属性。
     *
     * @param object     要处理的对象
     * @param visitedMap <b>已经访问过的对象MAP</b>。</br>
     *                   有两个作用，一是记录对象是否被访问过，避免循环引用；
     *                   二是记录原对象和处理后对象的关系。在In-place模式下，它主要用于防止循环引用，
     *                   在Append模式下，它同时还解决了循环引用导致的过期引用问题。
     * @return 处理后的对象
     */
    private Object processRecursively(Object object, Map<Object, Object> visitedMap) {
        // 终止条件：对象为空，或者是叶子类型的，即不是一个含有“有效”对象属性的对象，没有进一步处理的意义了。
        if (object == null || isLeafType(object.getClass())) {
            return object;
        }
        // 处理循环引用，如 A->B, B->A的循环。当B处理时发现A已在map中，会直接返回A的实例。
        // 如果仅仅这样，会导致过期引用的问题，需要采用两阶段转换方案（一阶段填充占位，二阶段填充真实值），即先创建一个占位对象，让对方持有
        // 然后再把真实数据放进去。
        if (visitedMap.containsKey(object)) {
            return visitedMap.get(object);
        }

        // 【第一阶段：创建并注册占位符】
        Object placeholder = createPlaceholder(object);
        visitedMap.put(object, placeholder);

        // 如果原对象是集合类型，遍历集合中的每个元素，依次处理后添加到占位集合中。
        if (object instanceof Collection) {
            // 填充数据到placeholder中
            return populateCollection(object, visitedMap, (Collection<Object>) placeholder);
        }

        // 如果原对象是Map类型，遍历每一个key-value对，将value处理后，添加到占位Map中
        if (object instanceof Map) {
            return populateMap(object, visitedMap, (Map<Object, Object>) placeholder);
        }

        // --- 核心POJO处理逻辑 ---
        // 构建class的元信息，时刻提醒有两种场景
        ClassMetadata classMetadata = CLASS_METADATA_CACHE.computeIfAbsent(object.getClass(), this::buildClassMetadata);
        // 子节点（属性）是否修改了，修改了就会将对象转成Map
        boolean hasChildrenChanged = false;
        Map<String, Object> processedProperties = new HashMap<>(classMetadata.getPropertiesToInspect().size());
        for (PropertyDescriptor pd : classMetadata.getPropertiesToInspect()) {
            try {
                Object propertyValue = pd.getReadMethod().invoke(object);
                Object e = processRecursively(propertyValue, visitedMap);
                processedProperties.put(pd.getName(), e);
                if (e != propertyValue) {
                    hasChildrenChanged = true;
                }
            } catch (Exception e) {
                // ignore
                log.warn("Failed to introspect property " + pd.getReadMethod().getName() + " of class " + object.getClass().getName());
                throw new ExchangeProcessingException("处理对象属性失败 " + pd.getReadMethod().getName(), e);
            }
        }

        //【第二阶段：填充占位符】
        Object finalObject = decideNodeTransformation(object, processedProperties, hasChildrenChanged);

        // 如果finalObject是一个Map (Append模式)，需要把数据填充到之前创建的placeholder中，而不是直接返回一个新的Map实例。
        if (placeholder instanceof Map && finalObject instanceof Map) {
            ((Map<String, Object>) placeholder).putAll((Map<String, Object>) finalObject);
            return placeholder;
        }
        return finalObject;
    }

    /**
     * 填充集合数据到placeholderList中
     *
     * @param originalCollection 原始集合对象
     * @param visitedMap         已访问对象Map
     * @param placeholderList    占位集合列表
     * @return 占位集合列表
     */
    private Object populateCollection(Object originalCollection, Map<Object, Object> visitedMap, Collection<Object> placeholderList) {
        for (Object item : (Collection<?>) originalCollection) {
            placeholderList.add(processRecursively(item, visitedMap));
        }
        return placeholderList;
    }

    /**
     * 填充数据到placeholderMap中
     *
     * @param originalMap    原始Map对象
     * @param visitedMap     已访问对象Map
     * @param placeholderMap 占位Map
     * @return 占位Map
     */
    private Object populateMap(Object originalMap, Map<Object, Object> visitedMap, Map<Object, Object> placeholderMap) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) originalMap).entrySet()) {
            placeholderMap.put(entry.getKey(), processRecursively(entry.getValue(), visitedMap));
        }
        return placeholderMap;
    }

    /**
     * 创建占位对象
     *
     * @param object 原对象
     * @return 占位对象
     */
    protected abstract Object createPlaceholder(Object object);

    /**
     * 是否是叶子类型
     *
     * @param clazz 类
     * @return 是/否
     */
    private boolean isLeafType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.getName().startsWith("java.lang") || clazz.getName().startsWith("java.math");
    }

    /**
     * 【决策核心】根据节点自身类型和其子节点的转换状态，决定此节点的最终形态。</br>
     * <b>INPLACE</b>: 原地修改模式中下，会直接返回原始对象</br>
     * <b>APPEND</b>: 追加模式下，要根据非叶子属性是否被处理过，和exchangeableFields是否为空来判断
     *
     * @param originalObject      原对象
     * @param processedProperties 包含了所有已被递归处理过的子属性的Map
     * @param hasChildrenChanged  如果为true，表示至少一个子节点从POJO被转换成了Map
     * @return 节点的最终形态 (可能是原始对象，也可能是一个新的Map)
     */
    protected abstract Object decideNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged);

    /**
     * 根据类信息构建类元信息
     *
     * @param clazz 类
     * @return 类元信息
     */
    final protected ClassMetadata buildClassMetadata(Class<?> clazz) {
        if (clazz.isPrimitive() || this.shouldSkipBuildMetadata(clazz) || clazz.isArray()) {
            return new ClassMetadata(false, Collections.emptyList(), Collections.emptyList(), null);
        }
        // 类是否实现了IApplyExchange接口
        boolean isApplyExchangeImplementor = IApplyExchange.class.isAssignableFrom(clazz);
        List<PropertyDescriptor> propertiesToInspect = new ArrayList<>();
        List<Field> exchangeFields = new ArrayList<>();
        Field baseCurrencyField = null;
        try {
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && !isLeafType(readMethod.getReturnType())) {
                    propertiesToInspect.add(propertyDescriptor);
                }
                // 检查属性上是否有AutoExchangeField注解
                Field field = findField(clazz, propertyDescriptor.getName());
                if (field != null) {
                    if (field.isAnnotationPresent(AutoExchangeField.class)) {
                        exchangeFields.add(field);
                    } else if (field.isAnnotationPresent(AutoExchangeBaseCurrency.class)) {
                        baseCurrencyField = field;
                    }
                }
            }
        } catch (Exception e) {
            log.error("buildClassMetadata error", e);
            throw new ExchangeProcessingException("构建类 " + clazz.getName() + " 的元信息失败");
        }
        return new ClassMetadata(isApplyExchangeImplementor, propertiesToInspect, exchangeFields, baseCurrencyField);
    }

    /**
     * 递归查找字段，包括父类的字段
     *
     * @param clazz 类
     * @param name  字段名称
     * @return 字段对象
     */
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

    /**
     * 根据类查询对应的类元信息
     *
     * @param clazz 类
     * @return 类元信息
     */
    final protected ClassMetadata getClassMetadata(Class<?> clazz) {
        return CLASS_METADATA_CACHE.computeIfAbsent(clazz, this::buildClassMetadata);
    }

    /**
     * 解析基础货币的方法，优先级是字段上指定的基础货币大于默认设置的基础货币
     *
     * @param object        对象
     * @param classMetadata 对象的元数据
     * @return 解析出的基础货币代码
     */
    protected final String resolveBaseCurrency(Object object, ClassMetadata classMetadata) {
        // 优先级从大到小：@AutoExchangeBaseCurrency > defaultCurrencyExchange
        Field baseCurrencyField = classMetadata.getBaseCurrencyField();
        if (baseCurrencyField != null) {
            try {
                baseCurrencyField.setAccessible(true);
                Object currencyValue = baseCurrencyField.get(object);
                if (currencyValue instanceof String && !((String) currencyValue).isEmpty()) {
                    return (String) currencyValue;
                }
            } catch (IllegalAccessException e) {
                log.error("解析@AutoExchangeBaseCurrency标注的属性值作为基础货币出错，", e);
                throw new AutoExchangeException("解析@AutoExchangeBaseCurrency标注的属性值作为基础货币出错，", e);
            }
        }
        return properties.getDefaultBaseCurrency();
    }

    /**
     * 定义生命周期钩子，可以做一些额外的动作，例如记录转换时间等
     */
    protected void beforeApplyExchange() {
    }

    /**
     * 定义生命周期钩子，可以做一些额外的动作，例如记录转换时间等
     */
    protected void afterApplyExchange() {
    }

    /**
     * 判断此类是否可以忽略判断
     * 默认跳过所有java.*包下的类，因为我们不需要深入遍历String, Date, BigDecimal等
     *
     * @param clazz 要检查的类
     * @return 是否需要跳过
     */
    protected boolean shouldSkipBuildMetadata(Class<?> clazz) {
        String className = clazz.getName();
        return className.startsWith("java.") || className.startsWith("javax.");
    }
}
