package tech.baizi.autoexchange.exception;

/**
 * 汇率没有找到的异常
 */
public class ExchangeRateNotFoundException extends AutoExchangeException {
    private final String baseCurrency;
    private final String targetCurrency;

    public ExchangeRateNotFoundException(String baseCurrency, String targetCurrency) {
        super("没有找到汇率数据，[baseCurrency]: " + baseCurrency + ", [targetCurrency]: " + targetCurrency);
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }
}
