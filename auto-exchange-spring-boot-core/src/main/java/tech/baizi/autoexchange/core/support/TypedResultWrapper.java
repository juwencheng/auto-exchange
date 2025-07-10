package tech.baizi.autoexchange.core.support;

/**
 * 包装APPEND模式下，返回结果中如果有循环依赖，无法正常序列化的返回结果包装类
 */
public class TypedResultWrapper {
    private final Object data;
    private final Class<?> originalType;

    public TypedResultWrapper(Object data, Class<?> originalType) {
        this.data = data;
        this.originalType = originalType;
    }

    public Object getData() {
        return data;
    }

    public Class<?> getOriginalType() {
        return originalType;
    }
}
