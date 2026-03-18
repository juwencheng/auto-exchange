package io.github.juwencheng.autoexchange.testapp;

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
import io.github.juwencheng.autoexchange.core.convertor.IExchangeResultDataConvertor;
import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;
import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.testapp.controller.TestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@TestPropertySource(properties = {
        "auto.exchange.refresh-on-launch=true",
        "auto.exchange.default-base-currency=CNY",
        "auto.exchange.rate-refresh.enabled=false"
})
@EnableScheduling
@EnableAspectJAutoProxy
public class CustomConvertorTest {

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
                            new ExchangeInfoRateDto("CNY", "CNY", BigDecimal.valueOf(1)),
                            new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(8))));
            return mock;
        }

        /**
         * 自定义汇率转换结果数据转换器，追加额外的 convertedBy 字段
         */
        @Bean
        @Primary
        public IExchangeResultDataConvertor customConvertor() {
            return exchangeResultDto -> {
                Map<String, Object> result = new HashMap<>();
                result.put("convertedPrice", exchangeResultDto.getPrice());
                result.put("currency", exchangeResultDto.getTransCurrency());
                result.put("convertedBy", "custom");
                return result;
            };
        }
    }

    @Test
    @DisplayName("测试自定义IExchangeResultDataConvertor可以替换默认的格式")
    void testCustomConvertor() throws Exception {
        mockMvc.perform(get("/test/simple")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.priceUsd").value(100.00))
                // 验证自定义格式的字段存在
                .andExpect(jsonPath("$.priceInCny.convertedPrice").value(100.00))
                .andExpect(jsonPath("$.priceInCny.currency").value("CNY"))
                .andExpect(jsonPath("$.priceInCny.convertedBy").value("custom"))
                // 验证默认格式的字段不存在
                .andExpect(jsonPath("$.priceInCny.base").doesNotExist())
                .andExpect(jsonPath("$.priceInCny.trans").doesNotExist())
                .andDo(print());
    }
}
