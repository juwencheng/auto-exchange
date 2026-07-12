package io.github.juwencheng.autoexchange.testapp.dto;

import io.github.juwencheng.autoexchange.exchange.ExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateField;

import java.math.BigDecimal;

public class Product {
    public Long id = 1L;
    public String name = "Test Product";

    @TranslateField(value = "priceInCny", translator = ExchangeFieldTranslator.class)
    public BigDecimal priceUsd = new BigDecimal("100.00");

    @TranslateField(translator = ExchangeFieldTranslator.class)
    public BigDecimal anotherPriceUsd = new BigDecimal("200.00");

    @ExchangeBaseCurrency
    private String currency;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public BigDecimal getAnotherPriceUsd() {
        return anotherPriceUsd;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
