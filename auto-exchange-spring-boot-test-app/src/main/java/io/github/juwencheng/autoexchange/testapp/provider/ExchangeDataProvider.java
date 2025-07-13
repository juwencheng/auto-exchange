package io.github.juwencheng.autoexchange.testapp.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExchangeDataProvider implements IExchangeDataProvider {
    private final Logger logger = LoggerFactory.getLogger(ExchangeDataProvider.class);
    @Override
    public List<ExchangeInfoRateDto> fetchData() {
        logger.info("开始刷新汇率数据");
        return List.of(new ExchangeInfoRateDto("USD", "CNY", BigDecimal.valueOf(7.3)));
    }

    @Override
    public List<ExchangeInfoRateDto> fetchData(LocalDateTime time) {
        return List.of();
    }
}
