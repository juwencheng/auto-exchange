package io.github.juwencheng.autoexchange.core.translate;

import java.lang.reflect.Field;

/**
 * 翻译字段的元数据，缓存注解解析结果以避免重复反射。
 *
 * @author juwencheng
 */
public class TranslateFieldMeta {

    private final Field field;
    private final String outputFieldName;
    private final Class<? extends FieldTranslator> translatorClass;
    private final String[] args;

    public TranslateFieldMeta(Field field, String outputFieldName, Class<? extends FieldTranslator> translatorClass, String[] args) {
        this.field = field;
        this.outputFieldName = outputFieldName;
        this.translatorClass = translatorClass;
        this.args = args;
    }

    public Field getField() {
        return field;
    }

    public String getOutputFieldName() {
        return outputFieldName;
    }

    public Class<? extends FieldTranslator> getTranslatorClass() {
        return translatorClass;
    }

    public String[] getArgs() {
        return args;
    }
}
