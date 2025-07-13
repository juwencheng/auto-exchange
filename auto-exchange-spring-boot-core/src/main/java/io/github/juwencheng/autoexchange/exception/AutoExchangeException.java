package io.github.juwencheng.autoexchange.exception;

/**
 * 本框架的顶级运行时异常
 * 所有在本框架中抛出的异常和业务中产生的异常都应继承自此类。
 */
public class AutoExchangeException extends RuntimeException{
    public AutoExchangeException(String message) {
        super(message);
    }

    public AutoExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
