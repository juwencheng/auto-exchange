package io.github.juwencheng.autoexchange.testapp.dto;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.math.BigDecimal;

/**
 * 用于测试 @AutoExchangeField 字段为 null 时的处理行为
 */
public class ProductWithNullPrice {
    public String name = "Null Price Product";

    @AutoExchangeField("priceInCny")
    public BigDecimal priceUsd = null;

    @AutoExchangeBaseCurrency
    public String currency = "USD";

    public String getName() {
        return name;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public String getCurrency() {
        return currency;
    }
}
