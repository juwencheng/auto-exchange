package io.github.juwencheng.autoexchange.core.translate.cache;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.translate.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateContext;

import java.time.Duration;

/**
 * 字典翻译默认缓存策略。
 * <p>
 * Key 格式：{@code dict:{dictType}:{key}}
 * 默认 TTL：1 小时（可通过配置 {@code auto.exchange.translate-cache.dict-ttl} 修改）
 *
 * @author juwencheng
 */
public class DictTranslateCacheStrategy implements TranslateCacheStrategy {

    private final AutoExchangeProperties properties;

    public DictTranslateCacheStrategy(AutoExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public Duration ttl() {
        return properties.getTranslateCache().getDictTtlDuration();
    }

    @Override
    public String buildKey(TranslateCacheKeyContext keyContext) {
        String[] args = keyContext.getArgs();
        String dictType = (args != null && args.length > 0) ? args[0] : "unknown";
        Object fieldValue = keyContext.getFieldValue();
        String key = fieldValue == null ? "null" : String.valueOf(fieldValue);
        return "dict:" + dictType + ":" + key;
    }
}
