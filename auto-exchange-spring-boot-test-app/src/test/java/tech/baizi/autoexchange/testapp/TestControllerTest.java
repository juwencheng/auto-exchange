package tech.baizi.autoexchange.testapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;
import tech.baizi.autoexchange.provider.IExchangeDataProvider;
import tech.baizi.autoexchange.testapp.controller.TestController;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@TestPropertySource(properties = {
        "auto.exchange.refresh-on-launch=true",
        "auto.exchange.default-base-currency=CNY",
        "auto.exchange.rate-refresh.enabled=false"
})
@EnableScheduling
// 启用 AOP，不显式指定Aspect无法生效
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
            // 设置默认行为
            when(mock.fetchData())
                    .thenReturn(List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(8)), new ExchangeInfoRateDto("CNY", "CNY", BigDecimal.valueOf(1))));
            return mock;
        }
    }


    @BeforeEach
    void setUp() {
        // 模拟汇率服务

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
                .andExpect(jsonPath("$.anotherPriceUsdAutoExchange.price").value(200.00))
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
                .andExpect(jsonPath("$.product.anotherPriceUsdAutoExchange.price").value(200.00))
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
                .andExpect(jsonPath("$.valueInCny.price").value(50.00))
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
                .andExpect(jsonPath("$.inventory.SKU-1.anotherPriceUsdAutoExchange.price").value(200.00))
                .andDo(print());
    }

    @Test
    @DisplayName("测试对象属性中有List的自动汇率转换")
    void testGetUserWishList() throws Exception{
        mockMvc.perform(get("/test/userWishList")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("USER-123"))
                .andExpect(jsonPath("$.items[0].name").value("Test Product"))
                .andExpect(jsonPath("$.items[0].anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.items[0].priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.items[0].anotherPriceUsdAutoExchange.price").value(200.00))
                .andExpect(jsonPath("$.items[1].name").value("Test Product"))
                .andExpect(jsonPath("$.items[1].anotherPriceUsd").value(200.00))
                .andExpect(jsonPath("$.items[1].priceInCny.price").value(100.00))
                .andExpect(jsonPath("$.items[1].anotherPriceUsdAutoExchange.price").value(200.00))
                .andDo(print());
    }


}
