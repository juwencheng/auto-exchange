package io.github.juwencheng.autoexchange.service;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;

import java.util.List;

@Deprecated
public class DefaultCurrencyExchangeService implements ICurrencyExchangeService {

    /**
     * 加载持久化的汇率数据
     *
     * @return 汇率数据列表
     */
    @Override
    public List<ExchangeInfoRateDto> loadPersistedRates() {
        return List.of();
    }

    /**
     * 保存汇率数据到数据库
     *
     * @param newRates 新的汇率数据
     * @return 是否保存成功
     */
    @Override
    public Boolean saveNewRates(List<ExchangeInfoRateDto> newRates) {
        return true;
    }

}
