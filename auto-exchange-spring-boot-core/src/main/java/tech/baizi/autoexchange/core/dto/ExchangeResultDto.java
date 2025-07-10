package tech.baizi.autoexchange.core.dto;

import java.math.BigDecimal;

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
}
