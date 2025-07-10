package tech.baizi.autoexchange.core.strategy.meta;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 类的元信息，包含汇率转换所用到的信息
 */
public class ClassMetadata {
    /**
     * 这个类是否可以转换，如果可以转换，转换此类的同时，还需要继续遍历它的属性中
     * 是否还有可以转换的类
     */
    private final boolean isApplyExchangeImplementor;
    /**
     * 需要进一步检查的属性列表
     */
    private final List<PropertyDescriptor> propertiesToInspect;

    /**
     * 需要进行汇率转换的字段
     */
    private final List<Field> exchangeableFields;


    public ClassMetadata(boolean isApplyExchangeImplementor, List<PropertyDescriptor> propertiesToInspect, List<Field> exchangeableFields) {
        this.isApplyExchangeImplementor = isApplyExchangeImplementor;
        this.propertiesToInspect = propertiesToInspect;
        this.exchangeableFields = exchangeableFields;
    }

    public boolean isApplyExchangeImplementor() {
        return isApplyExchangeImplementor;
    }

    public List<PropertyDescriptor> getPropertiesToInspect() {
        return propertiesToInspect;
    }


    public List<Field> getExchangeableFields() {
        return exchangeableFields;
    }
}
