package io.github.juwencheng.autoexchange.autoconfigure.annotation;

import org.springframework.context.annotation.Import;
import io.github.juwencheng.autoexchange.autoconfigure.AutoExchangeAutoConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AutoExchangeAutoConfiguration.class)
public @interface EnableAutoExchange {
}
