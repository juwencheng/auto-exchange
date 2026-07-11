package io.github.juwencheng.autoexchange.core.translate;

/**
 * 字典数据提供者接口。用于从字典表（或缓存）中获取 key 对应的 value。
 * <p>
 * 业务项目需要实现此接口，提供自己的字典数据源。
 *
 * @author juwencheng
 */
public interface IDictDataProvider {

    /**
     * 根据字典类型和 key 获取对应的 value
     *
     * @param dictType 字典类型名（如 "order_status"、"payment_type"）
     * @param key      字典项的 key 值
     * @return 对应的 value 值，未找到时返回 null
     */
    String getDictValue(String dictType, String key);
}
