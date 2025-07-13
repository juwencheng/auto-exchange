package io.github.juwencheng.autoexchange.processor.test.dto;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.math.BigDecimal;

public class ValidProductWithoutAutoExchangeBaseCurrency {
    private String currency;
    @AutoExchangeField
    private BigDecimal price;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
