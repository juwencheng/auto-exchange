package io.github.juwencheng.autoexchange.core.convertor;

import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;

import java.util.Map;

/**
 * 汇率转换结果数据转换器接口，允许用户自定义追加的汇率信息格式
 */
public interface IExchangeResultDataConvertor {
    /**
     * 将汇率转换结果转换为追加到响应中的数据格式
     *
     * @param exchangeResultDto 汇率转换结果
     * @return 追加到响应中的数据
     */
    Map<String, Object> convert(ExchangeResultDto exchangeResultDto);
}
