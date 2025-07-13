package io.github.juwencheng.autoexchange.core.strategy.meta;

import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeBaseCurrency;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 类的元信息，包含汇率转换所用到的信息
 */
public class ClassMetadata {
    /**
     * 类是否实现了IApplyExchange，如果实现了，需要使用INPLACE模式
     */
    private final boolean isApplyExchangeImplementor;
    /**
     * 需要进一步检查的属性列表
     * 注意，这里面都是非“叶子”属性，需要进一步检查的。
     */
    private final List<PropertyDescriptor> propertiesToInspect;

    /**
     * 需要进行汇率转换的字段列表，即有{@link AutoExchangeField}注解的属性
     */
    private final List<Field> exchangeableFields;

    /**
     * 存储被{@link AutoExchangeBaseCurrency}注解的字段
     */
    private final Field baseCurrencyField;


    public ClassMetadata(boolean isApplyExchangeImplementor, List<PropertyDescriptor> propertiesToInspect, List<Field> exchangeableFields, Field baseCurrencyField) {
        this.isApplyExchangeImplementor = isApplyExchangeImplementor;
        this.propertiesToInspect = propertiesToInspect;
        this.exchangeableFields = exchangeableFields;
        this.baseCurrencyField = baseCurrencyField;
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

    public Field getBaseCurrencyField() {
        return baseCurrencyField;
    }
}
