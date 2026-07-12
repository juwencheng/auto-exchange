package io.github.juwencheng.autoexchange.testapp;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.testapp.controller.TestController;
import io.github.juwencheng.fieldtranslate.dict.IDictDataProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import io.github.juwencheng.fieldtranslate.autoconfigure.FieldTranslateAutoConfiguration;
import io.github.juwencheng.autoexchange.autoconfigure.ExchangeAutoConfiguration;
import io.github.juwencheng.fieldtranslate.dict.autoconfigure.DictAutoConfiguration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@Import({FieldTranslateAutoConfiguration.class, ExchangeAutoConfiguration.class, DictAutoConfiguration.class})
@TestPropertySource(properties = {
        "auto.exchange.refresh-on-launch=true",
        "auto.exchange.default-base-currency=CNY",
        "auto.exchange.rate-refresh.enabled=false"
})
@EnableScheduling
@EnableAspectJAutoProxy
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.setThreadNamePrefix("test-scheduler-");
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        @Primary
        public IExchangeDataProvider mockExchangeRateService() {
            IExchangeDataProvider mock = Mockito.mock(IExchangeDataProvider.class);
            when(mock.fetchData())
                    .thenReturn(List.of(
                            new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(8)),
                            new ExchangeInfoRateDto("CNY", "USD", BigDecimal.valueOf(0.2)),
                            new ExchangeInfoRateDto("CNY", "CNY", BigDecimal.valueOf(1))));
            return mock;
        }

        @Bean
        @Primary
        public IDictDataProvider mockDictDataProvider() {
            return (dictType, key) -> {
                Map<String, Map<String, String>> data = new HashMap<>();
                Map<String, String> orderStatus = new HashMap<>();
                orderStatus.put("0", "待支付");
                orderStatus.put("1", "已支付");
                orderStatus.put("2", "已发货");
                orderStatus.put("3", "已完成");
                data.put("order_status", orderStatus);

                Map<String, String> paymentType = new HashMap<>();
                paymentType.put("ALIPAY", "支付宝");
                paymentType.put("WECHAT", "微信支付");
                data.put("payment_type", paymentType);

                Map<String, String> dict = data.get(dictType);
                return dict != null ? dict.get(key) : null;
            };
        }
    }

    @Test
    @DisplayName("测试简单产品对象的自动汇率转换")
    void testGetSimpleProduct() throws Exception {
        mockMvc.perform(get("/test/simple")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.priceUsd").value(100.00))
                .andExpect(jsonPath("$.anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.anotherPriceUsdTranslated.price").value(200.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试带 currency 参数的目标币种解析")
    void testGetSimpleProductWithCurrencyParam() throws Exception {
        mockMvc.perform(get("/test/simple?currency=USD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.priceUsd").value(100.00))
                .andExpect(jsonPath("$.anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.priceInCny.price").value(20.00))
                .andExpect(jsonPath("$.priceInCny.trans").value("USD"))
                .andExpect(jsonPath("$.anotherPriceUsdTranslated.price").value(40.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试嵌套产品对象的自动汇率转换")
    void testGetNestedOrder() throws Exception {
        mockMvc.perform(get("/test/nested")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.product.name").value("Test Product"))
                .andExpect(jsonPath("$.product.anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.product.priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.product.anotherPriceUsdTranslated.price").value(200.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试循环引用对象的自动汇率转换")
    void testGetCycleNode() throws Exception {
        mockMvc.perform(get("/test/cycle")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("parent"))
                .andExpect(jsonPath("$.value").value(50.00))
                .andExpect(jsonPath("$.child.name").value("child"))
                .andExpect(jsonPath("$.child.value").value(50.00))
                .andExpect(jsonPath("$.child.valueInCny.price").value(50.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试对象属性有Map的自动汇率转换")
    void testGetStoreInventory() throws Exception {
        mockMvc.perform(get("/test/inventory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.storeId").value("STORE-A"))
                .andExpect(jsonPath("$.inventory.SKU-1.name").value("Test Product"))
                .andExpect(jsonPath("$.inventory.SKU-1.anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.inventory.SKU-1.priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.inventory.SKU-1.anotherPriceUsdTranslated.price").value(200.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试对象属性中有List的自动汇率转换")
    void testGetUserWishList() throws Exception {
        mockMvc.perform(get("/test/userWishList")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("USER-123"))
                .andExpect(jsonPath("$.items[0].name").value("Test Product"))
                .andExpect(jsonPath("$.items[0].anotherPriceUsd").value(110.00))
                .andExpect(jsonPath("$.items[0].priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.items[0].anotherPriceUsdTranslated.price").value(110.00))
                .andExpect(jsonPath("$.items[1].name").value("Test Product"))
                .andExpect(jsonPath("$.items[1].anotherPriceUsd").value(210.00))
                .andExpect(jsonPath("$.items[1].priceInCny.price").value(200.00))
                .andExpect(jsonPath("$.items[1].anotherPriceUsdTranslated.price").value(210.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试汇率转换和字典翻译共存")
    void testOrderWithDictAndExchange() throws Exception {
        mockMvc.perform(get("/test/orderWithDict")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value("ORDER-100"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.paymentType").value("ALIPAY"))
                .andExpect(jsonPath("$.amountInCny.price").exists())
                .andExpect(jsonPath("$.amountInCny.base").value("USD"))
                .andExpect(jsonPath("$.amountInCny.trans").value("CNY"))
                .andExpect(jsonPath("$.statusText").value("已支付"))
                .andExpect(jsonPath("$.paymentTypeText").value("支付宝"))
                .andDo(print());
    }

    @Test
    @DisplayName("测试 @TranslateResponse 触发翻译")
    void testTranslateOnly() throws Exception {
        mockMvc.perform(get("/test/translateOnly")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value("ORDER-100"))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.paymentType").value("ALIPAY"))
                .andExpect(jsonPath("$.statusText").value("已支付"))
                .andExpect(jsonPath("$.paymentTypeText").value("支付宝"))
                .andExpect(jsonPath("$.amountInCny.price").exists())
                .andDo(print());
    }
}
