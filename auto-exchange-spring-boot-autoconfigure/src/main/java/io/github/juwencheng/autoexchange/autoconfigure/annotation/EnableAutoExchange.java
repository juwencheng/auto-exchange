package io.github.juwencheng.autoexchange.autoconfigure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 已废弃。请使用 {@code @EnableFieldTranslate}（field-translate）
 * 配合 {@code auto-exchange-spring-boot-starter} 自动配置。
 *
 * @author juwencheng
 * @deprecated 使用 {@code @EnableFieldTranslate} + starter 自动配置替代
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAutoExchange {
}
