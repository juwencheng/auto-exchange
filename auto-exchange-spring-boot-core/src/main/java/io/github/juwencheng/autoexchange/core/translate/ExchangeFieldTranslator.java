package io.github.juwencheng.autoexchange.core.translate;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;
import io.github.juwencheng.autoexchange.core.tools.BigDecimalTools;
import io.github.juwencheng.autoexchange.exception.ExchangeConfigurationException;
import io.github.juwencheng.autoexchange.exception.ExchangeRateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * 汇率转换翻译器。作为通用翻译框架的插件实现，将价格字段从一个币种转换为另一个币种。
 * <p>
 * 这是 {@link FieldTranslator} 的汇率转换实现，封装了原有 AutoApplyExchangeStrategy 中
 * 针对 @AutoExchangeField 的转换逻辑。
 *
 * @author juwencheng
 */
public class ExchangeFieldTranslator implements FieldTranslator {

    private static final Logger log = LoggerFactory.getLogger(ExchangeFieldTranslator.class);

    public static final String ATTR_TARGET_CURRENCY = "targetCurrency";

    private final ExchangeManager exchangeManager;
    private final AutoExchangeProperties properties;

    public ExchangeFieldTranslator(ExchangeManager exchangeManager, AutoExchangeProperties properties) {
        this.exchangeManager = exchangeManager;
        this.properties = properties;
    }

    @Override
    public Object translate(Object fieldValue, TranslateContext context) {
        String rawCurrency = context.getAttribute(ATTR_TARGET_CURRENCY);
        Object sourceObject = context.getAttribute("_sourceObject");

        final String targetCurrency = (rawCurrency == null || rawCurrency.isEmpty())
                ? properties.getDefaultTargetCurrency()
                : rawCurrency;

        String baseCurrency = resolveBaseCurrency(sourceObject);

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate(baseCurrency, targetCurrency);
        ExchangeResultDto exchangeResult = rate
                .map(rateDto -> new ExchangeResultDto(
                        rateDto,
                        BigDecimalTools.multiply(
                                BigDecimalTools.convertOrDefault(fieldValue, BigDecimal.ZERO),
                                rateDto.getRate())))
                .orElseGet(() -> resolveMissRate(fieldValue, baseCurrency, targetCurrency));

        return exchangeResult.toMap();
    }

    private String resolveBaseCurrency(Object object) {
        if (object == null) {
            return properties.getDefaultBaseCurrency();
        }
        Class<?> clazz = object.getClass();
        for (Field f : getAllFields(clazz)) {
            if (f.isAnnotationPresent(AutoExchangeBaseCurrency.class)) {
                try {
                    f.setAccessible(true);
                    Object value = f.get(object);
                    if (value instanceof String && !((String) value).isEmpty()) {
                        return (String) value;
                    }
                } catch (IllegalAccessException e) {
                    log.error("解析@AutoExchangeBaseCurrency失败", e);
                }
            }
        }
        return properties.getDefaultBaseCurrency();
    }

    private Field[] getAllFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return new Field[0];
        }
        Field[] declared = clazz.getDeclaredFields();
        Field[] parent = getAllFields(clazz.getSuperclass());
        Field[] all = new Field[declared.length + parent.length];
        System.arraycopy(declared, 0, all, 0, declared.length);
        System.arraycopy(parent, 0, all, declared.length, parent.length);
        return all;
    }

    private ExchangeResultDto resolveMissRate(Object fieldValue, String baseCurrency, String targetCurrency) {
        AutoExchangeProperties.MissingRate missingRate = this.properties.getMissingRate();
        switch (missingRate.getMissingRateStrategy()) {
            case THROW_EXCEPTION:
                throw new ExchangeRateNotFoundException(baseCurrency, targetCurrency);
            case PROTECTIVE:
                BigDecimal decimal = BigDecimalTools.multiply(
                        BigDecimalTools.convertOrDefault(fieldValue, BigDecimal.ZERO),
                        missingRate.getProtectiveRateValue());
                return new ExchangeResultDto(
                        new ExchangeInfoRateDto(baseCurrency, targetCurrency, missingRate.getProtectiveRateValue()),
                        decimal);
            case RETURN_NULL:
                return new ExchangeResultDto(
                        new ExchangeInfoRateDto(baseCurrency, targetCurrency, null), null);
        }
        throw new ExchangeConfigurationException("不支持的缺失汇率策略");
    }
}
