package tech.baizi.autoexchange.processor.test.dto;

import tech.baizi.autoexchange.core.annotation.AutoExchangeBaseCurrency;

public class MultiBaseCurrencyProduct {
    @AutoExchangeBaseCurrency
    private String currency;
    @AutoExchangeBaseCurrency
    private String currency1;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency1() {
        return currency1;
    }

    public void setCurrency1(String currency1) {
        this.currency1 = currency1;
    }
}
