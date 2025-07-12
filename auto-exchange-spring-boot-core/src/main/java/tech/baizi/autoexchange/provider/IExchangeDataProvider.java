package tech.baizi.autoexchange.provider;

import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;

import java.time.LocalDateTime;
import java.util.List;

public interface IExchangeDataProvider {

    List<ExchangeInfoRateDto> fetchData();

    List<ExchangeInfoRateDto> fetchData(LocalDateTime time);
}
