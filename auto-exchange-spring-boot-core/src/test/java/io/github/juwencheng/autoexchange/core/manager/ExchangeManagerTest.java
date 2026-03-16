package io.github.juwencheng.autoexchange.core.manager;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeManager 单元测试")
class ExchangeManagerTest {

    @Mock
    private IExchangeDataProvider dataProvider;

    private AutoExchangeProperties properties;
    private ExchangeManager exchangeManager;

    @BeforeEach
    void setUp() {
        properties = new AutoExchangeProperties();
        properties.setRefreshOnLaunch(false);
        exchangeManager = new ExchangeManager(dataProvider, properties);
    }

    @Test
    @DisplayName("init() 正常加载汇率数据")
    void init_shouldLoadRates() {
        when(dataProvider.fetchData()).thenReturn(
                List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.2)))
        );

        exchangeManager.init();

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate("USD", "CNY");
        assertThat(rate).isPresent();
        assertThat(rate.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(7.2));
    }

    @Test
    @DisplayName("init() 当 fetchData() 返回 null 时，应保留上次缓存而不抛异常")
    void init_whenFetchDataReturnsNull_shouldRetainPreviousCache() {
        // 先加载一次正常数据
        when(dataProvider.fetchData())
                .thenReturn(List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.2))))
                .thenReturn(null);

        exchangeManager.init();
        // 第二次 fetchData() 返回 null，缓存应保持
        exchangeManager.init();

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate("USD", "CNY");
        assertThat(rate).isPresent();
        assertThat(rate.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(7.2));
    }

    @Test
    @DisplayName("refreshRates() 当 fetchData() 返回 null 时，应保留上次缓存而不抛异常")
    void refreshRates_whenFetchDataReturnsNull_shouldRetainPreviousCache() {
        when(dataProvider.fetchData())
                .thenReturn(List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.2))))
                .thenReturn(null);

        exchangeManager.init();
        // 第二次 fetchData() 返回 null，缓存应保持
        exchangeManager.refreshRates();

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate("USD", "CNY");
        assertThat(rate).isPresent();
        assertThat(rate.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(7.2));
    }

    @Test
    @DisplayName("getRate() 未找到汇率时应返回 Optional.empty()")
    void getRate_whenRateNotFound_shouldReturnEmpty() {
        when(dataProvider.fetchData()).thenReturn(List.of());
        exchangeManager.init();

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate("USD", "EUR");
        assertThat(rate).isEmpty();
    }

    @Test
    @DisplayName("refreshRates() 更新汇率后应返回新数据")
    void refreshRates_shouldUpdateCache() {
        when(dataProvider.fetchData())
                .thenReturn(List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.2))))
                .thenReturn(List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.5))));

        exchangeManager.init();
        exchangeManager.refreshRates();

        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate("USD", "CNY");
        assertThat(rate).isPresent();
        assertThat(rate.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
    }
}
