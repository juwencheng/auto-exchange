package io.github.juwencheng.autoexchange.core.translate.cache;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.translate.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateContext;

import java.time.Duration;

/**
 * 汇率翻译结果默认缓存策略。
 * <p>
 * Key 格式：{@code exchange:{baseCurrency}:{targetCurrency}:{fieldValue}}
 * 默认 TTL：1 天（可通过配置 {@code auto.exchange.translate-cache.exchange-ttl} 修改）
 * <p>
 * 注意：汇率<b>数据源</b>的全量缓存仍由 {@link io.github.juwencheng.autoexchange.core.manager.ExchangeManager}
 * 管理（启动加载 + 定时刷新），本策略缓存的是<b>翻译结果</b>。
 *
 * @author juwencheng
 */
public class ExchangeTranslateCacheStrategy implements TranslateCacheStrategy {

    private final AutoExchangeProperties properties;

    public ExchangeTranslateCacheStrategy(AutoExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public Duration ttl() {
        return properties.getTranslateCache().getExchangeTtlDuration();
    }

    @Override
    public String buildKey(TranslateCacheKeyContext keyContext) {
        TranslateContext context = keyContext.getContext();
        String targetCurrency = context.getAttribute(ExchangeFieldTranslator.ATTR_TARGET_CURRENCY);
        if (targetCurrency == null) {
            targetCurrency = properties.getDefaultTargetCurrency();
        }

        Object sourceObject = context.getAttribute("_sourceObject");
        String baseCurrency = resolveBaseCurrency(sourceObject);

        Object fieldValue = keyContext.getFieldValue();
        String valuePart = fieldValue == null ? "null" : String.valueOf(fieldValue);

        return "exchange:" + baseCurrency + ":" + targetCurrency + ":" + valuePart;
    }

    private String resolveBaseCurrency(Object object) {
        if (object == null) {
            return properties.getDefaultBaseCurrency();
        }
        try {
            for (java.lang.reflect.Field f : object.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency.class)) {
                    f.setAccessible(true);
                    Object value = f.get(object);
                    if (value instanceof String && !((String) value).isEmpty()) {
                        return (String) value;
                    }
                }
            }
        } catch (IllegalAccessException ignored) {
        }
        return properties.getDefaultBaseCurrency();
    }
}
