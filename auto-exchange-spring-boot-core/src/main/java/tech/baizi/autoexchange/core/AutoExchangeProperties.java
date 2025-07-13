package tech.baizi.autoexchange.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.validation.annotation.Validated;
import tech.baizi.autoexchange.core.enums.MissingRateStrategy;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Validated // 启用JSR-303校验
@ConfigurationProperties(prefix = "auto.exchange")
public class AutoExchangeProperties {

    /**
     * 启动程序后，是否立即刷新汇率数据
     */
    private boolean refreshOnLaunch = true;

    /**
     * 默认全局基准货币，不指定，默认为人民币
     */
    private String defaultBaseCurrency = "CNY";
    /**
     * 默认的目标货币，不指定，默认为人民币
     */
    private String defaultTargetCurrency = "CNY";

    /**
     * 目标币种请求头名称
     */
    private String targetCurrencyHeaderName = "X-Target-Currency";

    /**
     * 目标币种参数名称
     */
    private String targetCurrencyParamName = "currency";


    /**
     * 切面的排序值（越大优先级越小），默认是LOWEST_PRECEDENCE(Integer.MAX_VALUE)
     */
    private Integer aspectOrder = Ordered.LOWEST_PRECEDENCE;
    /**
     * 刷新频率配置
     */
    private RateRefresh rateRefresh = new RateRefresh();

    private MissingRate missingRate = new MissingRate();

    public RateRefresh getRateRefresh() {
        return rateRefresh;
    }

    public void setRateRefresh(RateRefresh rateRefresh) {
        this.rateRefresh = rateRefresh;
    }

    public boolean isRefreshOnLaunch() {
        return refreshOnLaunch;
    }

    public void setRefreshOnLaunch(boolean refreshOnLaunch) {
        this.refreshOnLaunch = refreshOnLaunch;
    }

    public String getDefaultBaseCurrency() {
        return defaultBaseCurrency;
    }

    public String getDefaultTargetCurrency() {
        return defaultTargetCurrency;
    }

    public void setDefaultBaseCurrency(String defaultBaseCurrency) {
        this.defaultBaseCurrency = defaultBaseCurrency;
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

    public Integer getAspectOrder() {
        return aspectOrder;
    }

    public void setAspectOrder(Integer aspectOrder) {
        this.aspectOrder = aspectOrder;
    }

    /**
     * 缺少汇率值的配置
     */
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

    /**
     * 刷新汇率配置
     */
    public static class RateRefresh {
        /**
         * 是否启动自动刷新
         */
        private boolean enabled = false;


        /**
         * 刷新频率的cron表达式，默认每天凌晨0:30分刷新
         */
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

    public MissingRate getMissingRate() {
        return missingRate;
    }
}
