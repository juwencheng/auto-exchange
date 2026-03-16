package io.github.juwencheng.autoexchange.testapp;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.testapp.controller.TestController;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试缺失汇率策略：PROTECTIVE
 * 当目标货币无对应汇率时，使用预定义的保护性汇率值进行计算
 */
@WebMvcTest(TestController.class)
@TestPropertySource(properties = {
        "auto.exchange.refresh-on-launch=true",
        "auto.exchange.default-base-currency=CNY",
        "auto.exchange.rate-refresh.enabled=false",
        "auto.exchange.missing-rate.missing-rate-strategy=PROTECTIVE",
        "auto.exchange.missing-rate.protective-rate-value=10"
})
@EnableScheduling
@EnableAspectJAutoProxy
@DisplayName("缺失汇率策略测试：PROTECTIVE")
class MissingRateProtectiveTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.initialize();
            return scheduler;
        }

        @Bean
        @Primary
        public IExchangeDataProvider mockExchangeDataProvider() {
            IExchangeDataProvider mock = Mockito.mock(IExchangeDataProvider.class);
            when(mock.fetchData()).thenReturn(
                    List.of(new ExchangeInfoRateDto("CNY", "CNY", BigDecimal.ONE))
            );
            return mock;
        }
    }

    @Test
    @DisplayName("请求没有汇率的目标货币时，PROTECTIVE 策略应使用保护性汇率（10）计算价格")
    void testMissingRateUsesProtectiveRate() throws Exception {
        // Product.priceUsd 默认为 100.00，配置的保护汇率为 10，期望换算结果 price = 100 * 10 = 1000
        mockMvc.perform(get("/test/simple?currency=EUR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.priceInCny.price").value(1000.0))
                .andExpect(jsonPath("$.priceInCny.rate").value(10))
                .andExpect(jsonPath("$.priceInCny.trans").value("EUR"))
                .andDo(print());
    }
}
