package io.github.juwencheng.autoexchange.core.translate.cache;

import io.github.juwencheng.autoexchange.core.translate.FieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateContext;

import java.lang.reflect.Field;

/**
 * 构建缓存 key 时的上下文信息。
 *
 * @author juwencheng
 */
public class TranslateCacheKeyContext {

    private final Class<? extends FieldTranslator> translatorClass;
    private final Object fieldValue;
    private final TranslateContext context;
    private final String[] args;
    private final Field field;

    public TranslateCacheKeyContext(Class<? extends FieldTranslator> translatorClass,
                                    Object fieldValue,
                                    TranslateContext context,
                                    String[] args,
                                    Field field) {
        this.translatorClass = translatorClass;
        this.fieldValue = fieldValue;
        this.context = context;
        this.args = args;
        this.field = field;
    }

    public Class<? extends FieldTranslator> getTranslatorClass() {
        return translatorClass;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public TranslateContext getContext() {
        return context;
    }

    public String[] getArgs() {
        return args;
    }

    public Field getField() {
        return field;
    }
}
