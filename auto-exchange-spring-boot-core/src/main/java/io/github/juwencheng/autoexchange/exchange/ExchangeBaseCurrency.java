package io.github.juwencheng.autoexchange.exchange;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在 String 类型字段上，表示该字段的值是其所在对象中汇率字段的基础货币单位。
 *
 * @author juwencheng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExchangeBaseCurrency {
}
