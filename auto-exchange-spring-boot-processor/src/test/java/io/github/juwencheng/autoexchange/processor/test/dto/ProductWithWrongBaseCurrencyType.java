package io.github.juwencheng.autoexchange.processor.test.dto;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.math.BigDecimal;

public class ProductWithWrongBaseCurrencyType {
    @AutoExchangeBaseCurrency
    private Object currency;
    @AutoExchangeField
    private BigDecimal price;

    public Object getCurrency() {
        return currency;
    }

    public void setCurrency(Object currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
