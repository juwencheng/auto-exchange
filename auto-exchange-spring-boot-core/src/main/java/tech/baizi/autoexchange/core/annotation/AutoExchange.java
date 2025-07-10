package tech.baizi.autoexchange.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoExchange {
    // 表示汇率转换后的名字，在APPEND模式下会生效
    String name() default "";
}
