package io.github.juwencheng.autoexchange.testapp.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateField;

import java.math.BigDecimal;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class CycleNode {
    private String name;
    private CycleNode child;

    @TranslateField(value = "valueInCny", translator = ExchangeFieldTranslator.class)
    private BigDecimal value = new BigDecimal("50.00");

    public CycleNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CycleNode getChild() {
        return child;
    }

    public void setChild(CycleNode child) {
        this.child = child;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
