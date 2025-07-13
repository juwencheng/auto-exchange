package tech.baizi.autoexchange.processor.test.dto;

import tech.baizi.autoexchange.core.annotation.AutoExchangeField;

public class InValidExchangeFieldProduct {
    @AutoExchangeField
    private String inValidPrice;

    public String getInValidPrice() {
        return inValidPrice;
    }

    public void setInValidPrice(String inValidPrice) {
        this.inValidPrice = inValidPrice;
    }
}
