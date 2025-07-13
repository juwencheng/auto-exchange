package tech.baizi.autoexchange.testapp.controller;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;
import tech.baizi.autoexchange.core.annotation.AutoExchangeResponse;

import java.math.BigDecimal;

import static tech.baizi.autoexchange.testapp.dto.TestDtos.*;

@RestController
public class TestController {
    @GetMapping("/test/simple")
    @AutoExchangeResponse
    public Object getSimpleProduct() {
        return new Product();
    }

    @GetMapping("/test/nested")
    @AutoExchangeResponse
    public Order getNestedOrder() {
        return new Order();
    }

    // 为循环引用创建一个带@JsonIdentityInfo注解的新DTO
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
    public static class CycleNode {
        public String name;
        public CycleNode child;
        @AutoExchangeField("valueInCny")
        public BigDecimal value = new BigDecimal("50.00");
        public CycleNode(String name) { this.name = name; }
    }

    @GetMapping("/test/cycle")
    @AutoExchangeResponse
    public CycleNode getCyclicNode() {
        CycleNode parent = new CycleNode("parent");
        CycleNode child = new CycleNode("child");
        parent.child = child;
        child.child = parent; // A -> B -> A
        return parent;
    }
}
