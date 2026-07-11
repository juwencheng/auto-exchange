package io.github.juwencheng.autoexchange.core.translate;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用翻译上下文。承载请求级别的翻译参数和翻译结果。
 * <p>
 * 在 AOP 阶段：翻译器将翻译结果写入此上下文（通过 {@link #addAppendedData}）。
 * 在 Jackson 序列化阶段：序列化器从此上下文读取追加数据（通过 {@link #getAppendedDataFor}）。
 *
 * @author juwencheng
 */
public class TranslateContext {

    /**
     * 存储请求级别的属性（如目标币种、locale 等），供翻译器读取
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 存储需要追加到 JSON 中的翻译数据。
     * key 是对象引用（identity），value 是该对象上追加的字段 map
     */
    private final Map<Object, Map<String, Object>> appendedData = new HashMap<>();

    /**
     * 向上下文中设置属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 从上下文中获取属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 追加翻译数据到目标对象
     *
     * @param targetObject 目标对象（使用对象引用作为 key）
     * @param fieldName    追加的 JSON 字段名
     * @param data         翻译结果数据
     */
    public void addAppendedData(Object targetObject, String fieldName, Object data) {
        appendedData.computeIfAbsent(targetObject, k -> new HashMap<>()).put(fieldName, data);
    }

    /**
     * 获取目标对象上待追加的翻译数据
     *
     * @param targetObject 目标对象
     * @return 该对象上追加的字段 map，可能为 null
     */
    public Map<String, Object> getAppendedDataFor(Object targetObject) {
        return appendedData.get(targetObject);
    }

    /**
     * 获取所有属性
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
