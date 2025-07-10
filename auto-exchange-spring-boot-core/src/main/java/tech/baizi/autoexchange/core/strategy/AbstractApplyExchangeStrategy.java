package tech.baizi.autoexchange.core.strategy;

import tech.baizi.autoexchange.core.IApplyExchange;
import tech.baizi.autoexchange.core.annotation.AutoExchange;
import tech.baizi.autoexchange.core.strategy.meta.ClassMetadata;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 抽象的应用汇率策略，提供一些模版方法
 */
public abstract class AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    private final Logger logger = Logger.getLogger(AbstractApplyExchangeStrategy.class.getName());
    private final Map<Class<?>, ClassMetadata> classMetadataCache = new ConcurrentHashMap<>(100);

    @Override
    public Object applyExchange(Object rootObject) {
        if (rootObject == null) {
            return rootObject;
        }
        return processRecursively(rootObject, new IdentityHashMap<>());
    }

    /**
     * 循环处理对象，从底至上的方式处理每一个对象及其属性。
     *
     * @param object     要处理的对象
     * @param visitedMap 访问对象，两个作用，一是记录访问过的对象，二是记录原对象和处理后对象的关系，在inplace模式下
     *                   没有作用。但是在append模式下，会新增汇率转换的对象，那么就需要记录哪些属性处理成了新对象
     * @return 处理后的对象
     */
    private Object processRecursively(Object object, Map<Object, Object> visitedMap) {
        // 终止条件：对象为空，或者是叶子类型的，即不是一个含有“有效”对象属性的对象，没有进一步检测的意义了。
        if (object == null || isLeafType(object.getClass())) {
            return object;
        }
        // 处理循环引用：在复杂的业务模型中（如双向关联的JPA实体），循环引用非常常见。你的visitedMap机制是绝对必要的，不是理论上的
        if (visitedMap.containsKey(object)) {
            return visitedMap.get(object);
        }

        // 关键：在深入递归之前，先将当前对象放入visitedMap，值为自身。
        // 这可以正确处理A->B, B->A的循环。当B处理时发现A已在map中，会直接返回A的实例。
        // 还需要考虑引用过期的问题，使用两阶段逻辑实现，一阶段填充占位，二阶段填充真实值
        // 【第一阶段：创建并注册占位符】
        Object placeholder = createPlaceholder(object);
        visitedMap.put(object, placeholder);

        // 如果对象是集合类型，遍历集合中的每个元素，然后依次处理，并添加到新的集合中。
        if (object instanceof Collection) {
            // 填充数据到placeholder中
            return populateCollection(object, visitedMap, (Collection<Object>) placeholder);
        }

        // 如果是map类型，处理方式和集合类似
        if (object instanceof Map) {
            return populateMap(object, visitedMap, (Map<Object, Object>) placeholder);
        }

        // --- 核心POJO处理逻辑 ---
        // 构建class的元信息，时刻提醒有两种场景
        ClassMetadata classMetadata = classMetadataCache.computeIfAbsent(object.getClass(), this::buildClassMetadata);
        // 子节点是否修改了
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
                logger.warning("Failed to introspect property " + pd.getReadMethod().getName() + " of class " + object.getClass().getName());
            }
        }

        //【第二阶段：填充占位符】
        Object finalObject = decidedNodeTransformation(object, processedProperties, hasChildrenChanged);

        // 如果最终结果是一个Map (Append模式)，我们需要把数据填充到我们之前创建的placeholder中，
        // 而不是直接返回一个新的Map实例。
        if (placeholder instanceof Map && finalObject instanceof Map) {
            ((Map<String, Object>) placeholder).putAll((Map<String, Object>) finalObject);
            return placeholder;
        }

//        visitedMap.put(object, finalObject);
        return finalObject;
    }

    private Object populateCollection(Object originalCollection, Map<Object, Object> visitedMap, Collection<Object> placeholderList) {
        for (Object item : (Collection<?>) originalCollection) {
            placeholderList.add(processRecursively(item, visitedMap));
        }
        return placeholderList;
    }

    private Object populateMap(Object originalMap, Map<Object, Object> visitedMap, Map<Object, Object> placeholderMap) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) originalMap).entrySet()) {
            placeholderMap.put(entry.getKey(), processRecursively(entry.getValue(), visitedMap));
        }
        return placeholderMap;
    }

    protected abstract Object createPlaceholder(Object object);

    private boolean isLeafType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.getName().startsWith("java.lang") || clazz.getName().startsWith("java.math");
    }

    /**
     * 决定对象是否需要转换的方法
     *
     * @param originalObject      原对象
     * @param processedProperties 已经转换的属性key-value
     * @param hasChildrenChanged  属性是否被转换了
     * @return 如果需要转换，返回转换后的对象；否则返回原对象
     */
    protected abstract Object decidedNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged);

    private ClassMetadata buildClassMetadata(Class<?> clazz) {
        if (clazz.isPrimitive() || this.shouldSkipBuildMetadata(clazz) || clazz.isArray()) {
            return new ClassMetadata(false, Collections.emptyList(), Collections.emptyList());
        }
        // 判断类是不是需要进行汇率转化
        boolean isApplyExchangeImplementor = IApplyExchange.class.isAssignableFrom(clazz);
        List<PropertyDescriptor> propertiesToInspect = new ArrayList<>();
        List<Field> exchangeFields = new ArrayList<>();
        try {
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && !isLeafType(readMethod.getReturnType())) {
                    // 进一步判断属性是否需要进行转换
                    propertiesToInspect.add(propertyDescriptor);
                }
                // 检查属性上是否有AutoExchange注解
                Field field = findField(clazz, propertyDescriptor.getName());
                if (field != null && field.isAnnotationPresent(AutoExchange.class)) {
                    exchangeFields.add(field);
                }
            }
        } catch (Exception e) {
//            logger.log(System.Logger.Level.ERROR, "Failed to introspect class {}", clazz.getName(), e);
        }
        return new ClassMetadata(isApplyExchangeImplementor, propertiesToInspect, exchangeFields);
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

    final protected ClassMetadata getClassMetadata(Class<?> clazz) {
        return classMetadataCache.get(clazz);
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
     * 判断此类是否可以忽略
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
