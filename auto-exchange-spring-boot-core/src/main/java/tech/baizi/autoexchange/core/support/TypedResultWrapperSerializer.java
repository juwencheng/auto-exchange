package tech.baizi.autoexchange.core.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TypedResultWrapperSerializer extends JsonSerializer<TypedResultWrapper> {
    @Override
    public void serialize(TypedResultWrapper typedResultWrapper, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // 从上下文中获取当前的ObjectMapper
        ObjectMapper mapper = (ObjectMapper) jsonGenerator.getCodec();

        // 创建一个针对“原始类型”的特定ObjectWriter
        // 这个writer会加载并应用所有在originalType傻姑娘定义的Jackson注解（如@JsonIdentityInfo）。
        mapper.writerFor(typedResultWrapper.getOriginalType()).writeValue(jsonGenerator, typedResultWrapper.getData());
    }
}
