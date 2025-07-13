package io.github.juwencheng.autoexchange.provider;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;

import java.time.LocalDateTime;
import java.util.List;

public interface IExchangeDataProvider {

    List<ExchangeInfoRateDto> fetchData();

    List<ExchangeInfoRateDto> fetchData(LocalDateTime time);
}
