package io.github.juwencheng.autoexchange.core.translate;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

/**
 * 通用翻译框架的 Jackson BeanSerializerModifier。
 * 将所有可能需要追加翻译字段的 Bean 序列化器包装为 {@link TranslateAppendingBeanSerializer}。
 *
 * @author juwencheng
 */
public class TranslateBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (serializer instanceof BeanSerializerBase) {
            return new TranslateAppendingBeanSerializer((BeanSerializerBase) serializer);
        }
        return serializer;
    }
}
