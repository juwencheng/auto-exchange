package io.github.juwencheng.autoexchange.core.dto;

import java.math.BigDecimal;

public class ExchangeInfoRateDto {
    /**
     * 基础货币
     */
    protected String baseCurrency;
    /**
     * 目标货币
     */
    protected String transCurrency;
    /**
     * 汇率，关系是 目标货币 = 基础货币 * 汇率
     */
    protected BigDecimal rate;

    public ExchangeInfoRateDto() {

    }

    public ExchangeInfoRateDto(String baseCurrency, String transCurrency, BigDecimal rate) {
        this.baseCurrency = baseCurrency;
        this.transCurrency = transCurrency;
        this.rate = rate;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getTransCurrency() {
        return transCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public void setTransCurrency(String transCurrency) {
        this.transCurrency = transCurrency;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
