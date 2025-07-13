package io.github.juwencheng.autoexchange.testapp.controller;

import io.github.juwencheng.autoexchange.testapp.dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse;

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
