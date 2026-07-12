package io.github.juwencheng.autoexchange.core.translate.cache;

import java.time.Duration;

/**
 * 禁用缓存的策略，用于显式关闭某个字段的缓存。
 *
 * @author juwencheng
 */
public class NoCacheStrategy implements TranslateCacheStrategy {

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public Duration ttl() {
        return Duration.ZERO;
    }

    @Override
    public String buildKey(TranslateCacheKeyContext keyContext) {
        return "";
    }
}
