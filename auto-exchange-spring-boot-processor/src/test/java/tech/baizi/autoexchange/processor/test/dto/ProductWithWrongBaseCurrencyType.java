package tech.baizi.autoexchange.processor.test.dto;

import tech.baizi.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;

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
