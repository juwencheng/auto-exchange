package tech.baizi.autoexchange.exception;

/**
 * 处理汇率转换异常
 */
public class ExchangeProcessingException extends AutoExchangeException {
    public ExchangeProcessingException(String message) {
        super(message);
    }

    public ExchangeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
