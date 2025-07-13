package tech.baizi.autoexchange.testapp.dto;

// 包含嵌套对象
public class Order {
    private String orderId = "ORDER-001";
    private Product product = new Product();

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