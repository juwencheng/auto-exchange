package io.github.juwencheng.autoexchange.autoconfigure.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import io.github.juwencheng.autoexchange.exception.AutoExchangeException;
import io.github.juwencheng.autoexchange.exception.ExchangeRateNotFoundException;

import java.util.Map;

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
public class AutoExchangeExceptionHandler {
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
        // 通常这类问题是服务器端配置或内部错误
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
