package tech.baizi.autoexchange.core.serialize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

public class ExchangeBeanSerializerModifier extends BeanSerializerModifier {
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        // 检查这个类是否有可能需要附加属性
        // 我们可以优化：只有当一个类或其父类中存在@AutoExchangeField时，才包装它
        boolean mightNeedAppending = hasAutoExchangeFieldInHierarchy(beanDesc.getBeanClass());

        if (mightNeedAppending && serializer instanceof BeanSerializerBase) {
            return new AppendingBeanSerializer((BeanSerializerBase) serializer);
        }
        return serializer;
    }

    private boolean hasAutoExchangeFieldInHierarchy(Class<?> clazz) {
        // TODO: 这里可以有缓存，可以使用延时判断，也可以启动的时候就判断
        return true; // 简化：先假设所有都可能需要
    }
}