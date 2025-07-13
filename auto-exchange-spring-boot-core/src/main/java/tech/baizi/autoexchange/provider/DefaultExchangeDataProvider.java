package tech.baizi.autoexchange.provider;

import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 默认汇率数据提供者
 */
public class DefaultExchangeDataProvider implements IExchangeDataProvider {
    @Override
    public List<ExchangeInfoRateDto> fetchData() {
        return List.of(new ExchangeInfoRateDto("CNY", "USD", BigDecimal.valueOf(0.1404494382022472)));
    }

    @Override
    public List<ExchangeInfoRateDto> fetchData(LocalDateTime time) {
        return List.of();
    }
}
