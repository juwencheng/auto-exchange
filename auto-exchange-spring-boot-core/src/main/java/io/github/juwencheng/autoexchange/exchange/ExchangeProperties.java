package io.github.juwencheng.autoexchange.exchange;

import io.github.juwencheng.autoexchange.core.enums.MissingRateStrategy;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 汇率转换插件配置属性。
 *
 * @author juwencheng
 */
@Validated
@ConfigurationProperties(prefix = "auto.exchange")
public class ExchangeProperties {

    private boolean refreshOnLaunch = true;
    private String defaultBaseCurrency = "CNY";
    private String defaultTargetCurrency = "CNY";
    private String targetCurrencyHeaderName = "X-Target-Currency";
    private String targetCurrencyParamName = "currency";

    private final RateRefresh rateRefresh = new RateRefresh();
    private final MissingRate missingRate = new MissingRate();
    private final TranslateCache translateCache = new TranslateCache();

    public boolean isRefreshOnLaunch() {
        return refreshOnLaunch;
    }

    public void setRefreshOnLaunch(boolean refreshOnLaunch) {
        this.refreshOnLaunch = refreshOnLaunch;
    }

    public String getDefaultBaseCurrency() {
        return defaultBaseCurrency;
    }

    public void setDefaultBaseCurrency(String defaultBaseCurrency) {
        this.defaultBaseCurrency = defaultBaseCurrency;
    }

    public String getDefaultTargetCurrency() {
        return defaultTargetCurrency;
    }

    public void setDefaultTargetCurrency(String defaultTargetCurrency) {
        this.defaultTargetCurrency = defaultTargetCurrency;
    }

    public String getTargetCurrencyHeaderName() {
        return targetCurrencyHeaderName;
    }

    public void setTargetCurrencyHeaderName(String targetCurrencyHeaderName) {
        this.targetCurrencyHeaderName = targetCurrencyHeaderName;
    }

    public String getTargetCurrencyParamName() {
        return targetCurrencyParamName;
    }

    public void setTargetCurrencyParamName(String targetCurrencyParamName) {
        this.targetCurrencyParamName = targetCurrencyParamName;
    }

    public RateRefresh getRateRefresh() {
        return rateRefresh;
    }

    public MissingRate getMissingRate() {
        return missingRate;
    }

    public TranslateCache getTranslateCache() {
        return translateCache;
    }

    public static class MissingRate {
        private MissingRateStrategy missingRateStrategy = MissingRateStrategy.THROW_EXCEPTION;
        private BigDecimal protectiveRateValue = new BigDecimal("9999999.99");

        public MissingRateStrategy getMissingRateStrategy() {
            return missingRateStrategy;
        }

        public void setMissingRateStrategy(MissingRateStrategy missingRateStrategy) {
            this.missingRateStrategy = missingRateStrategy;
        }

        public BigDecimal getProtectiveRateValue() {
            return protectiveRateValue;
        }

        public void setProtectiveRateValue(BigDecimal protectiveRateValue) {
            this.protectiveRateValue = protectiveRateValue;
        }
    }

    public static class RateRefresh {
        private boolean enabled = false;

        @NotEmpty(message = "当启用汇率刷新时，CRON表达式不能为空")
        private String cron = "0 30 0 * * ?";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    /**
     * 汇率翻译结果缓存配置。
     */
    public static class TranslateCache {
        private boolean enabled = true;
        private Duration exchangeTtl = Duration.ofDays(1);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getExchangeTtl() {
            return exchangeTtl;
        }

        public void setExchangeTtl(Duration exchangeTtl) {
            this.exchangeTtl = exchangeTtl;
        }

        public Duration getExchangeTtlDuration() {
            return exchangeTtl != null ? exchangeTtl : Duration.ofDays(1);
        }
    }
}
