package tech.baizi.autoexchange.testapp.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;

import java.math.BigDecimal;

// 为循环引用创建一个带@JsonIdentityInfo注解的新DTO
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class CycleNode {
    private String name;
    private CycleNode child;
    @AutoExchangeField("valueInCny")
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