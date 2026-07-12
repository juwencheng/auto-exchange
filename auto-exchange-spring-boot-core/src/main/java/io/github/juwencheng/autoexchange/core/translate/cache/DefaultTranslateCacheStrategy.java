package io.github.juwencheng.autoexchange.core.translate.cache;

import java.time.Duration;

/**
 * 占位策略：表示使用翻译器注册的默认缓存策略。
 * 在 {@link TranslateCacheStrategyRegistry} 中解析为具体策略。
 *
 * @author juwencheng
 */
public class DefaultTranslateCacheStrategy implements TranslateCacheStrategy {

    @Override
    public Duration ttl() {
        return Duration.ZERO;
    }

    @Override
    public String buildKey(TranslateCacheKeyContext keyContext) {
        return "";
    }
}
