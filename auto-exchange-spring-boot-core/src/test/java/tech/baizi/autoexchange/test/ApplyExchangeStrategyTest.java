package tech.baizi.autoexchange.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tech.baizi.autoexchange.core.dto.ExchangeResultDto;
import tech.baizi.autoexchange.core.strategy.AppendApplyExchangeStrategy;
import tech.baizi.autoexchange.core.strategy.InPlaceApplyExchangeStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.baizi.autoexchange.test.dto.TestDtos.*;
@DisplayName("汇率转换策略单元测试")
public class ApplyExchangeStrategyTest {
    private AppendApplyExchangeStrategy appendStrategy;
    private InPlaceApplyExchangeStrategy inPlaceStrategy;

    @BeforeEach
    void setUp() {
        // 对于Append策略，我们需要一个ObjectMapper
        appendStrategy = new AppendApplyExchangeStrategy();

        // In-place策略没有外部依赖
        inPlaceStrategy = new InPlaceApplyExchangeStrategy();
    }

    // =========================================================================
    // 测试 Append (Annotation-based) 策略
    // =========================================================================
    @Nested
    @DisplayName("Append 策略 (基于注解)")
    class AppendStrategyTests {

        @Test
        @DisplayName("应处理null输入并返回null")
        void shouldReturnNullForNullInput() {
            Object result = appendStrategy.applyExchange(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("应对简单对象添加汇率信息")
        void shouldTransformSimpleObject() {
            Product product = new Product();
            Object result = appendStrategy.applyExchange(product);

            // 断言返回类型是Map
            assertThat(result).isInstanceOf(Map.class);
            Map<String, Object> resultMap = (Map<String, Object>) result;

            // 断言原始字段仍然存在
            assertThat(resultMap).containsEntry("id", 1L);
            assertThat(resultMap).containsEntry("name", "Test Product");
            assertThat(new BigDecimal(resultMap.get("priceUsd").toString())).isEqualByComparingTo("100.00");

            // 断言新的汇率字段被正确添加
            assertThat(resultMap).containsKey("priceInCny");
            ExchangeResultDto exchangeResultDto = (ExchangeResultDto)resultMap.get("priceInCny");
            assertThat(exchangeResultDto.getPrice()).isEqualByComparingTo("200.00"); // 模拟汇率是2
        }

        @Test
        @DisplayName("应对嵌套对象进行深度转换")
        void shouldTransformNestedObject() {
            Order order = new Order();
            Object result = appendStrategy.applyExchange(order);

            assertThat(result).isInstanceOf(Map.class);
            Map<String, Object> orderMap = (Map<String, Object>) result;
            assertThat(orderMap).containsEntry("orderId", "ORDER-001");

            // 检查嵌套的product是否也被转换成了Map
            assertThat(orderMap.get("product")).isInstanceOf(Map.class);
            Map<String, Object> productMap = (Map<String, Object>) orderMap.get("product");
            assertThat(productMap).containsKey("priceInCny");
        }

        @Test
        @DisplayName("应对集合中的每个对象进行转换")
        void shouldTransformObjectsInACollection() {
            UserWishlist wishlist = new UserWishlist();
            Object result = appendStrategy.applyExchange(wishlist);

            assertThat(result).isInstanceOf(Map.class);
            Map<String, Object> wishlistMap = (Map<String, Object>) result;

            // 检查集合本身是否存在且类型正确
            assertThat(wishlistMap.get("items")).isInstanceOf(List.class);
            List<Object> items = (List<Object>) wishlistMap.get("items");
            assertThat(items).hasSize(2);

            // 检查集合中的第一个元素是否被转换
            assertThat(items.get(0)).isInstanceOf(Map.class);
            Map<String, Object> firstItemMap = (Map<String, Object>) items.get(0);
            assertThat(firstItemMap).containsKey("priceInCny");
        }

        @Test
        @DisplayName("应对Map中的每个对象进行转换")
        void shouldTransformObjectsInAMap() {
            StoreInventory store = new StoreInventory();
            Object result = appendStrategy.applyExchange(store);

            assertThat(result).isInstanceOf(Map.class);
            Map<String, Object> storeMap = (Map<String, Object>) result;

            assertThat(storeMap.get("inventory")).isInstanceOf(Map.class);
            Map<String, Object> inventoryMap = (Map<String, Object>) storeMap.get("inventory");

            // 检查Map中的值是否被转换
            Object productInMap = inventoryMap.get("SKU-1");
            assertThat(productInMap).isInstanceOf(Map.class);
            assertThat((Map<String, Object>) productInMap).containsKey("priceInCny");
        }

        @Test
        @DisplayName("应正确处理循环引用而不会栈溢出")
        void shouldHandleCircularReferences() {
            Node parent = new Node("parent");
            Node child = new Node("child");
            parent.child = child;
            child.child = parent; // A -> B -> A

            Object result = appendStrategy.applyExchange(parent);

            // 测试没有抛出StackOverflowError就已成功大半
            assertThat(result).isInstanceOf(Map.class);
            Map<String, Object> parentMap = (Map<String, Object>) result;
            assertThat(parentMap.get("name")).isEqualTo("parent");
            assertThat(parentMap).containsKey("valueInCny");

            Map<String, Object> childMap = (Map<String, Object>) parentMap.get("child");
            assertThat(childMap.get("name")).isEqualTo("child");
            assertThat(childMap).containsKey("valueInCny");

            // 关键断言：孙子节点应该是对父节点的引用（在我们的实现里会是同一个Map实例）
            assertThat(childMap.get("child")).isSameAs(parentMap);
        }
    }

    // =========================================================================
    // 测试 In-place (Interface-based) 策略
    // =========================================================================
    @Nested
    @DisplayName("In-place 策略 (基于接口)")
    class InPlaceStrategyTests {

        @Test
        @DisplayName("应处理null输入并返回null")
        void shouldReturnNullForNullInput() {
            Object result = inPlaceStrategy.applyExchange(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("应对实现接口的简单对象进行原地修改")
        void shouldModifySimpleObjectInPlace() {
            ServicePlan plan = new ServicePlan();
            Object result = inPlaceStrategy.applyExchange(plan);

            // 断言返回的是原始对象实例
            assertThat(result).isSameAs(plan);

            // 断言对象内部状态已被修改
            ExchangeResultDto exchangeInfo = plan.getExchangeInfo();
            assertThat(exchangeInfo).isNotNull();
            assertThat(exchangeInfo.getPrice()).isEqualByComparingTo("199.98"); // 99.99 * 2
        }

        @Test
        @DisplayName("应对嵌套的实现接口的对象进行原地修改")
        void shouldModifyNestedObjectInPlace() {
            CustomerAccount account = new CustomerAccount();
            Object result = inPlaceStrategy.applyExchange(account);

            // 返回的是原始的account对象
            assertThat(result).isSameAs(account);

            // 检查嵌套的plan对象是否被修改
            ServicePlan plan = account.plan;
            assertThat(plan.getExchangeInfo()).isNotNull();
            assertThat(plan.getExchangeInfo().getPrice()).isEqualByComparingTo("199.98");
        }

        @Test
        @DisplayName("不应修改未实现接口的对象")
        void shouldNotModifyObjectNotImplementingInterface() throws JsonProcessingException {
            // Product类未实现IApplyExchange接口
            Product product = new Product();

            // 复制一份原始状态用于对比
            String originalState = new ObjectMapper().writeValueAsString(product);

            Object result = inPlaceStrategy.applyExchange(product);

            // 仍然返回原始对象
            assertThat(result).isSameAs(product);

            // 对象的JSON表示应未发生任何改变
            String finalState = new ObjectMapper().writeValueAsString(result);
            assertThat(finalState).isEqualTo(originalState);
        }
    }
}
