package io.github.juwencheng.autoexchange.core.translate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContext;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 统一的追加字段序列化器。同时从 {@link TranslateContext} 和 {@link AutoExchangeContext}
 * 读取追加数据，支持通用翻译框架和旧有汇率转换框架共存。
 *
 * @author juwencheng
 */
public class TranslateAppendingBeanSerializer extends BeanSerializer {

    public TranslateAppendingBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    public TranslateAppendingBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    public TranslateAppendingBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }

    public TranslateAppendingBeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(type, builder, properties, filteredProperties);
    }

    public TranslateAppendingBeanSerializer(BeanSerializerBase src, Set<String> toIgnore, Set<String> toInclude) {
        super(src, toIgnore, toInclude);
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new TranslateAppendingBeanSerializer(this, (Set<String>) null, (Set<String>) null);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new TranslateAppendingBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new TranslateAppendingBeanSerializer(this, _objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        if ((_objectIdWriter == null)
                && (_anyGetterWriter == null)
                && (_propertyFilterId == null)) {
            return new BeanAsArraySerializer(this);
        }
        return this;
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        super.serializeFields(bean, gen, provider);
        appendDynamicFields(bean, gen, provider);
    }

    private void appendDynamicFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try {
            // 从旧有的 AutoExchangeContext 中读取（向后兼容）
            AutoExchangeContext exchangeContext = AutoExchangeContextHolder.getContext();
            if (exchangeContext != null) {
                Map<String, Object> exchangeData = exchangeContext.getAppendedDataFor(bean);
                if (exchangeData != null && !exchangeData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : exchangeData.entrySet()) {
                        gen.writeObjectField(entry.getKey(), entry.getValue());
                    }
                }
            }

            // 从通用 TranslateContext 中读取
            TranslateContext translateContext = TranslateContextHolder.getContext();
            if (translateContext != null) {
                Map<String, Object> translateData = translateContext.getAppendedDataFor(bean);
                if (translateData != null && !translateData.isEmpty()) {
                    for (Map.Entry<String, Object> entry : translateData.entrySet()) {
                        gen.writeObjectField(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            provider.reportMappingProblem(e, "Failed to append dynamic fields for %s", bean.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return "TranslateAppendingBeanSerializer for " + handledType().getName();
    }
}
