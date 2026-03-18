package io.github.juwencheng.autoexchange.core.convertor;

import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;

import java.util.Map;

/**
 * 默认的汇率转换结果数据转换器，返回包含 base、trans、rate、price 字段的 Map
 */
public class DefaultExchangeResultDataConvertor implements IExchangeResultDataConvertor {
    @Override
    public Map<String, Object> convert(ExchangeResultDto exchangeResultDto) {
        return exchangeResultDto.toMap();
    }
}
