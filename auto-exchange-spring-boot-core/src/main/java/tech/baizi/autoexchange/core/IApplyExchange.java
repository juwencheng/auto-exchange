package tech.baizi.autoexchange.core;

import java.math.BigDecimal;

public interface IApplyExchange {
    /**
     * 应用汇率转换
     *
     * @param targetCurrency 目标币种
     * @param rate           汇率
     */
    void applyExchange(String targetCurrency, BigDecimal rate);
}
