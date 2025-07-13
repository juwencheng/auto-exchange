package io.github.juwencheng.autoexchange.testapp.dto;

import java.util.List;

// 包含集合
public class UserWishlist {
    private String userId = "USER-123";
    private List<Product> items = List.of(new Product(), new Product());

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Product> getItems() {
        return items;
    }

    public void setItems(List<Product> items) {
        this.items = items;
    }
}