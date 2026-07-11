package io.github.juwencheng.autoexchange.core.translate;

/**
 * 字段翻译器接口。所有翻译逻辑（汇率转换、字典翻译等）的统一抽象。
 * <p>
 * 每个实现类负责一种特定类型的翻译。框架通过 {@link TranslateField#translator()}
 * 来确定使用哪个翻译器。
 *
 * @author juwencheng
 */
public interface FieldTranslator {

    /**
     * 执行翻译逻辑
     *
     * @param fieldValue 原始字段值
     * @param context    翻译上下文，包含请求级别的上下文信息
     * @return 翻译后的结果，将被追加到 JSON 输出中
     */
    Object translate(Object fieldValue, TranslateContext context);
}
