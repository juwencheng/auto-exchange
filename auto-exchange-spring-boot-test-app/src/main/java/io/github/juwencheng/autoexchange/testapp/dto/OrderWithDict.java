package io.github.juwencheng.autoexchange.testapp.dto;

import io.github.juwencheng.autoexchange.exchange.ExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateField;
import io.github.juwencheng.fieldtranslate.dict.DictFieldTranslator;

import java.math.BigDecimal;

/**
 * 演示 DTO：同时使用汇率转换和字典翻译。
 *
 * @author juwencheng
 */
public class OrderWithDict {

    private String orderId = "ORDER-100";

    @TranslateField(value = "amountInCny", translator = ExchangeFieldTranslator.class)
    private BigDecimal amount = new BigDecimal("500.00");

    @ExchangeBaseCurrency
    private String currency = "USD";

    @TranslateField(value = "statusText", translator = DictFieldTranslator.class, args = "order_status")
    private Integer status = 1;

    @TranslateField(value = "paymentTypeText", translator = DictFieldTranslator.class, args = "payment_type")
    private String paymentType = "ALIPAY";

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getStatus() {
        return status;
    }

    public String getPaymentType() {
        return paymentType;
    }
}
