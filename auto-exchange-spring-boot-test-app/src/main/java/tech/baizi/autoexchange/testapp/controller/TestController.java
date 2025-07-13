package tech.baizi.autoexchange.testapp.controller;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;
import tech.baizi.autoexchange.core.annotation.AutoExchangeResponse;
import tech.baizi.autoexchange.testapp.dto.*;

import java.math.BigDecimal;

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

    @GetMapping("/test/cycle")
    @AutoExchangeResponse
    public CycleNode getCyclicNode() {
        CycleNode parent = new CycleNode("parent");
        CycleNode child = new CycleNode("child");
        parent.setChild(child);
        child.setChild(parent); // A -> B -> A
        return parent;
    }

    @GetMapping("test/inventory")
    @AutoExchangeResponse
    public StoreInventory getStoreInventory() {
        return new StoreInventory();
    }

    @GetMapping("test/userWishList")
    @AutoExchangeResponse
    public UserWishlist getUserWishlist() {
        return new UserWishlist();
    }
}
