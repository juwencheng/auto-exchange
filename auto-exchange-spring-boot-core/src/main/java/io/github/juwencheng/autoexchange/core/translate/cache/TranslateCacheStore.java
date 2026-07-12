package io.github.juwencheng.autoexchange.core.translate.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * 翻译缓存存储抽象。框架默认提供内存实现，业务可自定义 Redis 等实现并注册为 Spring Bean。
 * <p>
 * 通过 {@link TranslateCacheStrategy#storeName()} 与存储实例的 {@link #name()} 匹配绑定。
 *
 * @author juwencheng
 */
public interface TranslateCacheStore {

    /**
     * 存储实例名称，如 "memory"、"redis"
     */
    String name();

    /**
     * 读取缓存
     */
    Optional<Object> get(String key);

    /**
     * 写入缓存
     *
     * @param key   缓存 key
     * @param value 缓存值
     * @param ttl   过期时间，null 或 zero 表示不过期
     */
    void put(String key, Object value, Duration ttl);

    /**
     * 删除指定 key
     */
    void evict(String key);

    /**
     * 按前缀批量删除（可选实现，默认逐 key 不支持）
     */
    default void evictByPrefix(String prefix) {
        // 默认空实现，Redis 等实现可覆盖
    }
}
