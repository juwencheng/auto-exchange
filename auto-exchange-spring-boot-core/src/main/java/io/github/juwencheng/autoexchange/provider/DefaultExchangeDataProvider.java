package io.github.juwencheng.autoexchange.provider;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 默认汇率数据提供者
 */
public class DefaultExchangeDataProvider implements IExchangeDataProvider {
    @Override
    public List<ExchangeInfoRateDto> fetchData() {
        return List.of();
    }

    @Override
    public List<ExchangeInfoRateDto> fetchData(LocalDateTime time) {
        return List.of();
    }
}
