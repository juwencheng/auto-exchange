package io.github.juwencheng.autoexchange.testapp.dto;

import io.github.juwencheng.autoexchange.core.IApplyExchange;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.math.BigDecimal;
import java.util.Optional;

public class Product implements IApplyExchange {
    public Long id = 1L;
    public String name = "Test Product";
    @AutoExchangeField("priceInCny")
    public BigDecimal priceUsd = new BigDecimal("100.00");
    @AutoExchangeField
    public BigDecimal anotherPriceUsd = new BigDecimal("200.00");

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

    @Override
    public void applyExchange(String targetCurrency, Optional<BigDecimal> rate) {
        this.anotherPriceUsd = anotherPriceUsd.multiply(rate.orElse(BigDecimal.ZERO));
    }
}











