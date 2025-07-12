package tech.baizi.autoexchange.service;

import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;

import java.util.List;

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
