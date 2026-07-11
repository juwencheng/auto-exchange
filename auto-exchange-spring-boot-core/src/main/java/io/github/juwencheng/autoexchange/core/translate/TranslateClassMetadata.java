package io.github.juwencheng.autoexchange.core.translate;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * 通用翻译框架的类元数据，缓存每个类中需要翻译的字段和需要递归检查的属性。
 *
 * @author juwencheng
 */
public class TranslateClassMetadata {

    /**
     * 需要进一步递归检查的属性列表（非叶子类型的属性）
     */
    private final List<PropertyDescriptor> propertiesToInspect;

    /**
     * 需要翻译的字段列表（标注了 @TranslateField 的字段）
     */
    private final List<TranslateFieldMeta> translateFields;

    public TranslateClassMetadata(List<PropertyDescriptor> propertiesToInspect, List<TranslateFieldMeta> translateFields) {
        this.propertiesToInspect = propertiesToInspect;
        this.translateFields = translateFields;
    }

    public List<PropertyDescriptor> getPropertiesToInspect() {
        return propertiesToInspect;
    }

    public List<TranslateFieldMeta> getTranslateFields() {
        return translateFields;
    }

    public boolean hasTranslateFields() {
        return translateFields != null && !translateFields.isEmpty();
    }
}
