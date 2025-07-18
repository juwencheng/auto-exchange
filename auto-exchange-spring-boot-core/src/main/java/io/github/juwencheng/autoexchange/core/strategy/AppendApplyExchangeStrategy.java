package io.github.juwencheng.autoexchange.core.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;
import io.github.juwencheng.autoexchange.core.strategy.meta.ClassMetadata;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * 弃用了，本来是用来做APPEND模式的实际处理类的，结果在做集成测试的时候发现类型转换错误，于是想了新的方法来解决。，见{@link AutoApplyExchangeStrategy}
 */
@Deprecated
public class AppendApplyExchangeStrategy extends AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    private static final Logger log = LoggerFactory.getLogger(AppendApplyExchangeStrategy.class);

    public AppendApplyExchangeStrategy(AutoExchangeProperties properties) {
        super(properties);
    }

    @Override
    protected Object createPlaceholder(Object object) {
        if (object instanceof Collection) {
            return new LinkedList<>();
        } else if (object instanceof Map) {
            return new HashMap<>(((Map<?, ?>) object).size());
        }
        return new LinkedHashMap<>();
    }

    @Override
    protected Object decideNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged) {
        ClassMetadata classMetadata = getClassMetadata(originalObject.getClass());

        if (classMetadata == null || (!hasChildrenChanged && classMetadata.getExchangeableFields().isEmpty())) {
            return originalObject;
        }
        Map<String, Object> resultMap = createShallowMapFromObject(originalObject, classMetadata);
        for (Field field : classMetadata.getExchangeableFields()) {
            try {
                field.setAccessible(true);
                Object originalValue = field.get(originalObject);
                if (originalValue == null) {
                    continue;
                }
                AutoExchangeField annotation = field.getAnnotation(AutoExchangeField.class);
                if (annotation == null) {
                    continue;
                }
                BigDecimal val = new BigDecimal(originalValue.toString());
                ExchangeResultDto exchangeResultDto = new ExchangeResultDto();
                exchangeResultDto.setPrice(val.multiply(BigDecimal.valueOf(2)));
                resultMap.put(annotation.value(), exchangeResultDto);
            } catch (Exception e) {
                // 吃掉
//                throw new RuntimeException(e);
            }
        }
        resultMap.putAll(processedProperties);
        return resultMap;
    }

    /**
     * 将一个对象浅层转换为Map，只包含其直接的原始类型或叶子节点属性。
     * 这个方法不会递归，因此可以安全地用于有循环引用的对象。
     *
     * @param object        要转换的对象
     * @param classMetadata 对象的元数据
     * @return 一个包含对象属性的Map
     */
    private Map<String, Object> createShallowMapFromObject(Object object, ClassMetadata classMetadata) {
        Map<String, Object> map = new HashMap<>();
        try {
            // TODO: 这个也可以缓存起来
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors()) {
                // 读取属性
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && !"getClass".equals(readMethod.getName())) {
                    try {
                        map.put(propertyDescriptor.getName(), readMethod.invoke(object));
                    } catch (Exception e) {
                        log.warn("无法读取属性: " + propertyDescriptor.getName(), e);
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
