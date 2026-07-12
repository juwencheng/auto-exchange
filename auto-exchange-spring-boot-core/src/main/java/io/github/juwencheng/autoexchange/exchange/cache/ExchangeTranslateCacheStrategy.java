package io.github.juwencheng.autoexchange.exchange.cache;

import io.github.juwencheng.autoexchange.exchange.ExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.exchange.ExchangeProperties;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateContext;
import io.github.juwencheng.fieldtranslate.core.translate.cache.TranslateCacheKeyContext;
import io.github.juwencheng.fieldtranslate.core.translate.cache.TranslateCacheStrategy;

import java.time.Duration;

/**
 * 汇率翻译结果默认缓存策略。
 * <p>
 * Key 格式：{@code exchange:{baseCurrency}:{targetCurrency}:{fieldValue}}
 *
 * @author juwencheng
 */
public class ExchangeTranslateCacheStrategy implements TranslateCacheStrategy {

    private final ExchangeProperties properties;

    public ExchangeTranslateCacheStrategy(ExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean enabled() {
        return properties.getTranslateCache().isEnabled();
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
                if (f.isAnnotationPresent(ExchangeBaseCurrency.class)) {
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
