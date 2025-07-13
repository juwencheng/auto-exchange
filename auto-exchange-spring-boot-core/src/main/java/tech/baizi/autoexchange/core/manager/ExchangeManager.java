package tech.baizi.autoexchange.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.dto.ExchangeInfoRateDto;
import tech.baizi.autoexchange.provider.IExchangeDataProvider;
import tech.baizi.autoexchange.service.ICurrencyExchangeService;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExchangeManager implements ApplicationListener<ApplicationReadyEvent> {
    Logger log = LoggerFactory.getLogger(ExchangeManager.class);
    private final IExchangeDataProvider dataProvider;
    private final AtomicReference<Map<String, ExchangeInfoRateDto>> cache = new AtomicReference<>();
    private final boolean refreshOnLaunch;

    public ExchangeManager(IExchangeDataProvider dataProvider, AutoExchangeProperties properties) {
        this.dataProvider = dataProvider;
        this.refreshOnLaunch = properties.isRefreshOnLaunch();
        // 确保不为空
        this.cache.set(Collections.emptyMap());
    }

    public void init() {
        // 从数据库中读取数据
        List<ExchangeInfoRateDto> rateDtos = dataProvider.fetchData();
        // 保存到数据库
        Map<String, ExchangeInfoRateDto> newMap = rateDtos.stream().collect(Collectors.toMap((e) -> (e.getBaseCurrency() + "-" + e.getTransCurrency()), Function.identity()));
        cache.set(newMap);
    }

    public void refreshRates() {
        List<ExchangeInfoRateDto> rateDtos = dataProvider.fetchData();
        if (rateDtos == null) {
            log.warn("refreshRates 失败，返回结果为空");
            // DISCUSSION: 如果为null，推测汇率更新程序出错了，保留上一次数据，但也可以清空cache，这里选择保留方案。
            return;
        }
        Map<String, ExchangeInfoRateDto> newMap = rateDtos.stream().collect(Collectors.toMap((e) -> e.getBaseCurrency() + "-" + e.getTransCurrency(), Function.identity()));
        cache.set(newMap);
    }

    public Optional<ExchangeInfoRateDto> getRate(String baseCurrency, String transCurrency) {
        ExchangeInfoRateDto exchangeInfoRateDto = cache.get().get(String.format("%s-%s", baseCurrency, transCurrency));
        return Optional.ofNullable(exchangeInfoRateDto);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (this.refreshOnLaunch) {
            log.info("启动时从刷新汇率数据...");
            init();
        }
    }
}
