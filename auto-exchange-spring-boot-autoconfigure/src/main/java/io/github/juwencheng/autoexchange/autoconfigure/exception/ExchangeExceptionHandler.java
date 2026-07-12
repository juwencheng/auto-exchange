package io.github.juwencheng.autoexchange.autoconfigure.exception;

import io.github.juwencheng.autoexchange.exception.AutoExchangeException;
import io.github.juwencheng.autoexchange.exception.ExchangeRateNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 汇率转换异常处理器。
 *
 * @author juwencheng
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
public class ExchangeExceptionHandler {

    @ExceptionHandler(ExchangeRateNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRateNotFound(ExchangeRateNotFoundException ex) {
        Map<String, Object> errorBody = Map.of(
                "success", false,
                "message", ex.getMessage(),
                "details", Map.of("base", ex.getBaseCurrency(), "target", ex.getTargetCurrency())
        );
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AutoExchangeException.class)
    public ResponseEntity<Map<String, Object>> handleGenericExchangeException(AutoExchangeException ex) {
        Map<String, Object> errorBody = Map.of(
                "success", false,
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
