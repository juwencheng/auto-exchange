package io.github.juwencheng.autoexchange.testapp.controller;

import io.github.juwencheng.autoexchange.testapp.dto.*;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/simple")
    @TranslateResponse
    public Object getSimpleProduct() {
        return new Product();
    }

    @GetMapping("/test/nested")
    @TranslateResponse
    public Order getNestedOrder() {
        return new Order();
    }

    @GetMapping("/test/cycle")
    @TranslateResponse
    public CycleNode getCyclicNode() {
        CycleNode parent = new CycleNode("parent");
        CycleNode child = new CycleNode("child");
        parent.setChild(child);
        child.setChild(parent);
        return parent;
    }

    @GetMapping("test/inventory")
    @TranslateResponse
    public StoreInventory getStoreInventory() {
        return new StoreInventory();
    }

    @GetMapping("test/userWishList")
    @TranslateResponse
    public UserWishlist getUserWishlist() {
        return new UserWishlist();
    }

    @GetMapping("/test/orderWithDict")
    @TranslateResponse
    public OrderWithDict getOrderWithDict() {
        return new OrderWithDict();
    }

    @GetMapping("/test/translateOnly")
    @TranslateResponse
    public OrderWithDict getTranslateOnly() {
        return new OrderWithDict();
    }
}
