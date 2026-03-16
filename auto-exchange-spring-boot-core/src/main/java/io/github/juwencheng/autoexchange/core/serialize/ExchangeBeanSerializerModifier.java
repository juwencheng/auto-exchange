package io.github.juwencheng.autoexchange.core.serialize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeBeanSerializerModifier extends BeanSerializerModifier {
    private final Map<Class<?>, Boolean> cache = new ConcurrentHashMap<>(64);

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        boolean mightNeedAppending = hasAutoExchangeFieldInHierarchy(beanDesc.getBeanClass());

        if (mightNeedAppending && serializer instanceof BeanSerializerBase) {
            return new AppendingBeanSerializer((BeanSerializerBase) serializer);
        }
        return serializer;
    }

    private boolean hasAutoExchangeFieldInHierarchy(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, this::inspectHierarchy);
    }

    private boolean inspectHierarchy(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(AutoExchangeField.class)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }
}