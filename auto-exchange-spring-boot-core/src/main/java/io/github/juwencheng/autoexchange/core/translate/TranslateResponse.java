package io.github.juwencheng.autoexchange.core.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用翻译响应注解。标注在 Controller 方法上，触发通用翻译框架处理。
 * <p>
 * 与 {@link io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse} 的关系：
 * AutoExchangeResponse 是面向汇率转换的专用注解，会同时触发汇率转换和通用翻译。
 * TranslateResponse 是纯通用注解，只触发通用翻译框架（@TranslateField 标注的字段）。
 *
 * @author juwencheng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TranslateResponse {
}
