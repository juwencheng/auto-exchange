package tech.baizi.autoexchange.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;
import tech.baizi.autoexchange.service.CurrencyExchangeService;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExchangeManager implements ApplicationListener<ApplicationReadyEvent> {
    Logger log = LoggerFactory.getLogger(ExchangeManager.class);
    private final CurrencyExchangeService currencyExchangeService;
    private final AtomicReference<Map<String, ExchangeInfoRateDto>> cache = new AtomicReference<>();
    private final boolean refreshOnLaunch;

    public ExchangeManager(CurrencyExchangeService currencyExchangeService, AutoExchangeProperties properties) {
        this.currencyExchangeService = currencyExchangeService;
        this.refreshOnLaunch = properties.isRefreshOnLaunch();
        // 确保不为空
        this.cache.set(Collections.emptyMap());
    }

    public void init() {
        // 从数据库中读取数据
        List<ExchangeInfoRateDto> rateDtos = currencyExchangeService.getRatesFromDatabase();
//        // 保存到数据库
        Map<String, ExchangeInfoRateDto> newMap = rateDtos.stream().collect(Collectors.toMap((e) -> String.format("%s-%s", e.getBaseCurrency(), e.getTransCurrency()), Function.identity()));
        cache.set(newMap);
    }

    public void refreshRates() {
        List<ExchangeInfoRateDto> rateDtos = currencyExchangeService.refreshRates();
        if (rateDtos == null || rateDtos.isEmpty()) {
            log.error("refreshRates 失败，返回结果为空");
            return;
        }
        Map<String, ExchangeInfoRateDto> newMap = rateDtos.stream().collect(Collectors.toMap((e) -> String.format("%s-%s", e.getBaseCurrency(), e.getTransCurrency()), Function.identity()));
        cache.set(newMap);
        // 保存数据到数据库
    }

    public ExchangeInfoRateDto getRate(String baseCurrency, String transCurrency) {
        return cache.get().get(String.format("%s-%s", baseCurrency, transCurrency));
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (this.refreshOnLaunch) {
            log.info("启动时刷新汇率数据...");
            init();
        }
    }
}
