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
 * 测试缺失汇率策略：RETURN_NULL
 * 当目标货币无对应汇率时，转换后的价格字段应为 null
 */
@WebMvcTest(TestController.class)
@TestPropertySource(properties = {
        "auto.exchange.refresh-on-launch=true",
        "auto.exchange.default-base-currency=CNY",
        "auto.exchange.rate-refresh.enabled=false",
        "auto.exchange.missing-rate.missing-rate-strategy=RETURN_NULL"
})
@EnableScheduling
@EnableAspectJAutoProxy
@DisplayName("缺失汇率策略测试：RETURN_NULL")
class MissingRateReturnNullTest {

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
    @DisplayName("请求没有汇率的目标货币时，RETURN_NULL 策略应返回 null 价格")
    void testMissingRateReturnsNull() throws Exception {
        mockMvc.perform(get("/test/simple?currency=EUR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.priceInCny.price").doesNotExist())
                .andExpect(jsonPath("$.priceInCny.trans").value("EUR"))
                .andDo(print());
    }
}
