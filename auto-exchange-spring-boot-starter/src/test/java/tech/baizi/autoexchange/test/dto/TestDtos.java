package tech.baizi.autoexchange.test.dto;

import tech.baizi.autoexchange.core.IApplyExchange;
import tech.baizi.autoexchange.core.annotation.AutoExchangeField;
import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;
import tech.baizi.autoexchange.core.dto.ExchangeResultDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 辅助类，方便测试
public class TestDtos {

    // ==== For Append Strategy ====

    // 基础产品，带有一个需要转换的字段
    public static class Product {
        public Long id = 1L;
        public String name = "Test Product";
        @AutoExchangeField("priceInCny")
        public BigDecimal priceUsd = new BigDecimal("100.00");

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPriceUsd() {
            return priceUsd;
        }
    }

    // 包含嵌套对象
    public static class Order {
        public String orderId = "ORDER-001";
        public Product product = new Product();

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }
    }

    // 包含集合
    public static class UserWishlist {
        public String userId = "USER-123";
        public List<Product> items = List.of(new Product(), new Product());

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

    // 包含Map
    public static class StoreInventory {
        public String storeId = "STORE-A";
        public Map<String, Product> inventory = Map.of("SKU-1", new Product());

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

    // 循环引用
    public static class Node {
        public String name;
        public Node child;
        @AutoExchangeField("valueInCny")
        public BigDecimal value = new BigDecimal("50.00");

        public Node(String name) { this.name = name; }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Node getChild() {
            return child;
        }

        public void setChild(Node child) {
            this.child = child;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    // ==== For In-place Strategy ====

    public static class ServicePlan implements IApplyExchange {
        public String planName = "Premium Plan";
        public BigDecimal monthlyFee = new BigDecimal("99.99");
        private ExchangeResultDto exchangeInfo;

        @Override
        public void applyExchange(String currency, Optional<BigDecimal> rateOpt) {
            BigDecimal rate = rateOpt.orElse(BigDecimal.ZERO);
            BigDecimal convertedPrice = this.monthlyFee.multiply(rate);
            ExchangeInfoRateDto exchangeInfoRateDto = new ExchangeInfoRateDto();
            exchangeInfoRateDto.setRate(rate);
            this.exchangeInfo = new ExchangeResultDto(exchangeInfoRateDto, convertedPrice);
        }

        // Getter for testing
        public ExchangeResultDto getExchangeInfo() { return exchangeInfo; }
    }

    public static class CustomerAccount {
        public String accountId = "ACC-456";
        public ServicePlan plan = new ServicePlan();

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public ServicePlan getPlan() {
            return plan;
        }

        public void setPlan(ServicePlan plan) {
            this.plan = plan;
        }
    }
}