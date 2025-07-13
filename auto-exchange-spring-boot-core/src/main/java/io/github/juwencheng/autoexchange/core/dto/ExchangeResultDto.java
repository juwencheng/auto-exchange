package io.github.juwencheng.autoexchange.core.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ExchangeResultDto extends ExchangeInfoRateDto {
    /**
     * 兑换后价格
     */
    private BigDecimal price;

    public ExchangeResultDto() {
    }

    public ExchangeResultDto(ExchangeInfoRateDto exchangeInfoRateDto, BigDecimal price) {
        this.baseCurrency = exchangeInfoRateDto.baseCurrency;
        this.transCurrency = exchangeInfoRateDto.transCurrency;
        this.rate = exchangeInfoRateDto.rate;
        this.price = price;
    }

    public static ExchangeResultDto calculate(ExchangeInfoRateDto exchangeInfoRateDto, BigDecimal originalPrice) {
        return new ExchangeResultDto(exchangeInfoRateDto, originalPrice.multiply(exchangeInfoRateDto.getRate()));
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>() {{
            put("base", baseCurrency);
            put("trans", transCurrency);
            put("rate", rate);
            put("price", price);
        }};
    }
}
