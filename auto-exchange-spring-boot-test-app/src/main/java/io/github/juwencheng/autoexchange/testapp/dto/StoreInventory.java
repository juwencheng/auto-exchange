package io.github.juwencheng.autoexchange.testapp.dto;

import java.util.Map;

// 包含Map
public class StoreInventory {
    private String storeId = "STORE-A";
    private Map<String, Product> inventory = Map.of("SKU-1", new Product());

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Map<String, Product> getInventory() {
        return inventory;
    }

    public void setInventory(Map<String, Product> inventory) {
        this.inventory = inventory;
    }
}