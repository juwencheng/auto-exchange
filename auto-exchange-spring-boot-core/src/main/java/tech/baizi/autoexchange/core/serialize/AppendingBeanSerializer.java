package tech.baizi.autoexchange.core.serialize;

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
import tech.baizi.autoexchange.core.context.AutoExchangeContext;
import tech.baizi.autoexchange.core.context.AutoExchangeContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class AppendingBeanSerializer extends BeanSerializer {
    public AppendingBeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    public AppendingBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    public AppendingBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }

    public AppendingBeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(type, builder, properties, filteredProperties);
    }

    public AppendingBeanSerializer(BeanSerializerBase src, Set<String> toIgnore, Set<String> toInclude) {
        super(src, toIgnore, toInclude);
    }

    // BeanSerializerModifier会调用这个方法来创建一个新的实例，这是递归的关键
    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new AppendingBeanSerializer(this, (Set<String>) null, (Set<String>) null);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        // 当处理@JsonIdentityInfo时，确保返回的序列化器仍然是我们的包装类型
        return new AppendingBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new AppendingBeanSerializer(this, _objectIdWriter, filterId);
    }


    @Override
    protected BeanSerializerBase asArraySerializer() {
        if ((_objectIdWriter == null)
                && (_anyGetterWriter == null)
                && (_propertyFilterId == null)
        ) {
            return new BeanAsArraySerializer(this);
        }
        return this;
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 1. 【首先】调用父类的原始方法，让它正常地序列化所有静态字段。
        //    这会正确地处理所有嵌套对象的递归序列化。
        super.serializeFields(bean, gen, provider);

        // 2. 【然后】在所有常规字段都已写入后，追加我们的动态字段。
        appendDynamicFields(bean, gen, provider);
    }

    /**
     * 将动态计算的字段写入JSON流。
     */
    private void appendDynamicFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try {
            AutoExchangeContext context = AutoExchangeContextHolder.getContext();
            if (context == null) {
                return;
            }

            Map<String, Object> appendedData = context.getAppendedDataFor(bean);
            if (appendedData != null && !appendedData.isEmpty()) {
                for (Map.Entry<String, Object> entry : appendedData.entrySet()) {
                    // 使用JsonGenerator来写入新的字段和值
                    gen.writeObjectField(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            // 记录日志或处理异常，避免影响整个序列化过程
            provider.reportMappingProblem(e, "Failed to append dynamic fields for %s", bean.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return "AppendingBeanSerializer for " + handledType().getName();
    }
}
