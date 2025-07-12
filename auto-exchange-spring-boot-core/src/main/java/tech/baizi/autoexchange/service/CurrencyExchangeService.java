package tech.baizi.autoexchange.service;

import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;
import tech.baizi.autoexchange.provider.IExchangeDataProvider;

import java.util.List;

public class CurrencyExchangeService {
    private final IExchangeDataProvider exchangeDataProvider;

    public CurrencyExchangeService(IExchangeDataProvider exchangeDataProvider) {
        this.exchangeDataProvider = exchangeDataProvider;
    }

    public List<ExchangeInfoRateDto> refreshRates() {
        return exchangeDataProvider.fetchData();
    }

    public List<ExchangeInfoRateDto> getRatesFromDatabase() {
        return null;
    }
}
