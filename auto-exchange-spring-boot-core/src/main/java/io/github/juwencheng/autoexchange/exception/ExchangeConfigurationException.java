package io.github.juwencheng.autoexchange.exception;

/**
 * 配置相关错误
 */
public class ExchangeConfigurationException extends AutoExchangeException {
    public ExchangeConfigurationException(String message) {
        super(message);
    }

    public ExchangeConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
