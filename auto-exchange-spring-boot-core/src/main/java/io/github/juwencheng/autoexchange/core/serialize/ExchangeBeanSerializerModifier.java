package io.github.juwencheng.autoexchange.core.serialize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

/**
 * @deprecated 请使用 {@link io.github.juwencheng.autoexchange.core.translate.TranslateBeanSerializerModifier}，
 * 它同时支持旧有的 AutoExchangeContext 和新的 TranslateContext。
 */
@Deprecated
public class ExchangeBeanSerializerModifier extends BeanSerializerModifier {
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        boolean mightNeedAppending = hasAutoExchangeFieldInHierarchy(beanDesc.getBeanClass());

        if (mightNeedAppending && serializer instanceof BeanSerializerBase) {
            return new AppendingBeanSerializer((BeanSerializerBase) serializer);
        }
        return serializer;
    }

    private boolean hasAutoExchangeFieldInHierarchy(Class<?> clazz) {
        return true;
    }
}