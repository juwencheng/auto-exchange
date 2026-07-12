package io.github.juwencheng.autoexchange.testapp.dto;

import java.util.List;

// 包含集合
public class UserWishlist {
    private String userId = "USER-123";
    private List<Product> items = List.of(createProduct(1L, "100.00", "110.00"), createProduct(2L, "200.00", "210.00"));

    private static Product createProduct(Long id, String priceUsd, String anotherPrice) {
        Product product = new Product();
        product.id = id;
        product.priceUsd = new java.math.BigDecimal(priceUsd);
        product.anotherPriceUsd = new java.math.BigDecimal(anotherPrice);
        return product;
    }

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