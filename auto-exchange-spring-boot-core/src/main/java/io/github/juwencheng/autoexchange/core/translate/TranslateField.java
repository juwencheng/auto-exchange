package io.github.juwencheng.autoexchange.core.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用字段翻译注解。标注在字段上，表示此字段需要进行翻译处理。
 * 不同的翻译器（FieldTranslator）负责不同的翻译逻辑（如汇率转换、字典翻译等）。
 *
 * @author juwencheng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TranslateField {

    /**
     * 在返回的JSON中，追加的翻译结果字段名。
     * 如果为空，默认使用 "{字段名}Translated"。
     *
     * @return 翻译结果字段名
     */
    String value() default "";

    /**
     * 翻译器类型，指定使用哪个 FieldTranslator 实现来处理此字段。
     *
     * @return 翻译器的 Class
     */
    Class<? extends FieldTranslator> translator();

    /**
     * 翻译参数，传递给翻译器的额外参数。
     * 例如字典翻译时可以传入字典类型名。
     *
     * @return 翻译参数数组
     */
    String[] args() default {};

    /**
     * 缓存策略。默认 {@link io.github.juwencheng.autoexchange.core.translate.cache.DefaultTranslateCacheStrategy}，
     * 表示使用该翻译器注册的默认缓存策略。
     * 设为 {@link io.github.juwencheng.autoexchange.core.translate.cache.NoCacheStrategy} 可显式禁用缓存。
     *
     * @return 缓存策略类
     */
    Class<? extends io.github.juwencheng.autoexchange.core.translate.cache.TranslateCacheStrategy> cacheStrategy()
            default io.github.juwencheng.autoexchange.core.translate.cache.DefaultTranslateCacheStrategy.class;
}
