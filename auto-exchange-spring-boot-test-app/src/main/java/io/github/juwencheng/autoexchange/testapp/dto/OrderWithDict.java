package io.github.juwencheng.autoexchange.testapp.dto;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import io.github.juwencheng.autoexchange.core.translate.DictFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateField;

import java.math.BigDecimal;

/**
 * 演示 DTO：同时使用旧的 @AutoExchangeField 和新的 @TranslateField 注解。
 * 展示汇率转换和字典翻译两种翻译器在同一对象上共存的能力。
 *
 * @author juwencheng
 */
public class OrderWithDict {

    private String orderId = "ORDER-100";

    @AutoExchangeField("amountInCny")
    private BigDecimal amount = new BigDecimal("500.00");

    @AutoExchangeBaseCurrency
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
