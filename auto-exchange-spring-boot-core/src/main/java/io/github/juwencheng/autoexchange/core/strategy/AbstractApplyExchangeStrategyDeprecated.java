package io.github.juwencheng.autoexchange.core.strategy;

import io.github.juwencheng.autoexchange.core.strategy.meta.ClassMetadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 抽象的应用汇率策略，提供一些模版方法
 */
public abstract class AbstractApplyExchangeStrategyDeprecated implements IApplyExchangeStrategy {
    private final Logger logger = Logger.getLogger(AbstractApplyExchangeStrategyDeprecated.class.getName());
    private final Map<Class<?>, ClassMetadata> classMetadataCache = new ConcurrentHashMap<>(100);

    @Override
    public Object applyExchange(Object rootObject) {
        if (rootObject == null) {
            return rootObject;
        }
        // 确保是通过==对比对象，而不是常规set的equals来对比对象
        final Set<Object> visitedObjects = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Object> convertibleObjects = Collections.newSetFromMap(new IdentityHashMap<>());
        final Queue<Object> queue = new LinkedList<>();
        queue.add(rootObject);
        while (!queue.isEmpty()) {
            Object currentObject = queue.poll();
            if (currentObject == null || visitedObjects.contains(currentObject)) {
                continue;
            }
            traverseObject(currentObject, queue, convertibleObjects);
        }
        beforeApplyExchange();
        for (Object convertibleObject : convertibleObjects) {
            doApplyExchange(convertibleObject);
        }
        return rootObject;
    }

    protected abstract void doApplyExchange(Object convertibleObject);

    /**
     * 广度优先算法遍历对象及其属性中是否有可以应用汇率的类
     *
     * @param object             对象
     * @param queue              带检查对象队列
     * @param convertibleObjects 可以转换的对象集合
     */
    private void traverseObject(Object object, Queue<Object> queue, Set<Object> convertibleObjects) {
        ClassMetadata classMetadata = classMetadataCache.computeIfAbsent(object.getClass(), this::buildClassMetadata);
        if (classMetadata.isApplyExchangeImplementor()) {
            convertibleObjects.add(object);
        }
        // 如果是集合类型
        if (object instanceof Collection) {
            ((Collection<?>) object).forEach(queue::offer);
        } else if (object instanceof Map) {
            ((Map<?, ?>) object).values().forEach(queue::offer);
        }
        // 遍历字段
//        for (Method method : classMetadata.getPropertiesToInspect()) {
//            try {
//                Object propertyValue = method.invoke(object);
//                queue.offer(propertyValue);
//            } catch (Exception e) {
//                // ignore
//                logger.warning("Failed to introspect property " + method.getName() + " of class " + object.getClass().getName());
//            }
//        }

    }

    private ClassMetadata buildClassMetadata(Class<?> clazz) {
        return null;
//        if (clazz.isPrimitive() || this.shouldSkipBuildMetadata(clazz)) {
//            return new ClassMetadata(false, Collections.emptyList(), true);
//        }
//        if (clazz.isArray()) {
//            return new ClassMetadata(false, Collections.emptyList(), false);
//        }
//        boolean isConvertible = isConvertible(clazz);
//        List<Method> propertiesToInspect = new ArrayList<>();
//        try {
//            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
//                Method readMethod = propertyDescriptor.getReadMethod();
//                if (readMethod != null) {
//                    Class<?> returnType = readMethod.getReturnType();
//                    if (!returnType.isPrimitive()) {
//                        propertiesToInspect.add(readMethod);
//                    }
//                }
//            }
//        } catch (Exception e) {
////            logger.log(System.Logger.Level.ERROR, "Failed to introspect class {}", clazz.getName(), e);
//        }
//        return new ClassMetadata(isConvertible, propertiesToInspect, !isConvertible && propertiesToInspect.isEmpty());
    }

    /**
     * 是否可以转换
     *
     * @return
     */
    protected abstract boolean isConvertible(Class<?> clazz);

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
