package io.github.juwencheng.autoexchange.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoExchangeField {
    /**
     * 在返回的JSON中，新增的汇率信息对象的字段名。
     * 这是value()的别名。
     * <b>注意：</b>不能和原字段名字一样，一样会覆盖原来字段。
     * @return 新字段名
     */
    String value() default "";
}
