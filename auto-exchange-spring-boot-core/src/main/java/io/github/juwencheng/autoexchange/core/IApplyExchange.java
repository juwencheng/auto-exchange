package io.github.juwencheng.autoexchange.core;

import java.math.BigDecimal;
import java.util.Optional;

public interface IApplyExchange {
    /**
     * 应用汇率转换
     *
     * @param targetCurrency 目标币种
     * @param rateOpt        汇率可能为空
     */
    void applyExchange(String targetCurrency, Optional<BigDecimal> rateOpt);
}
