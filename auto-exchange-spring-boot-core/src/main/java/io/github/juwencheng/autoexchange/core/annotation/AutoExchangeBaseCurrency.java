package io.github.juwencheng.autoexchange.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在String类型的字段上，表示该字段的值是其所在对象中{@link AutoExchangeField}标记的价格字段的基础货币单位。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoExchangeBaseCurrency {
}
