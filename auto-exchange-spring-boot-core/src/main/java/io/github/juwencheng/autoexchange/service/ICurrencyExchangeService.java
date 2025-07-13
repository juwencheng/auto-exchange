package io.github.juwencheng.autoexchange.service;

import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;

import java.util.List;

/**
 * 经过考虑，不在此框架中做数据持久
 */
@Deprecated
public interface ICurrencyExchangeService {
    /**
     * 加载持久化的汇率数据
     *
     * @return 汇率数据列表
     */
    List<ExchangeInfoRateDto> loadPersistedRates();

    /**
     * 保存汇率数据到数据库
     *
     * @param newRates 新的汇率数据
     * @return 是否保存成功
     */
    Boolean saveNewRates(List<ExchangeInfoRateDto> newRates);
}
