package io.github.juwencheng.autoexchange.core.translate.cache;

import java.time.Duration;

/**
 * 翻译缓存策略。定义是否启用缓存、使用哪个存储、TTL 以及如何构建 cache key。
 * <p>
 * 可在 {@link io.github.juwencheng.autoexchange.core.translate.TranslateField#cacheStrategy()} 上
 * 为单个字段指定；未指定时使用翻译器注册的默认策略。
 *
 * @author juwencheng
 */
public interface TranslateCacheStrategy {

    /**
     * 是否启用缓存
     */
    default boolean enabled() {
        return true;
    }

    /**
     * 使用的缓存存储名称，与 {@link TranslateCacheStore#name()} 匹配。
     * 默认 "memory"。
     */
    default String storeName() {
        return InMemoryTranslateCacheStore.STORE_NAME;
    }

    /**
     * 缓存过期时间
     */
    Duration ttl();

    /**
     * 构建缓存 key。建议包含 namespace、翻译器标识和业务维度，避免冲突。
     * <p>
     * 示例：{@code dict:order_status:1}、{@code exchange:USD:CNY:100.00}
     */
    String buildKey(TranslateCacheKeyContext keyContext);
}
