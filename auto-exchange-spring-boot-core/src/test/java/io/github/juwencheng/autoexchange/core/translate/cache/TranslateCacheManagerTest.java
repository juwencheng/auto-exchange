package io.github.juwencheng.autoexchange.core.translate.cache;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.translate.DictFieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.FieldTranslator;
import io.github.juwencheng.autoexchange.core.translate.TranslateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author juwencheng
 */
class TranslateCacheManagerTest {

    private TranslateCacheManager cacheManager;
    private AtomicInteger translateCount;

    @BeforeEach
    void setUp() {
        AutoExchangeProperties properties = new AutoExchangeProperties();
        properties.getTranslateCache().setDictTtl(Duration.ofHours(1));

        DictTranslateCacheStrategy dictStrategy = new DictTranslateCacheStrategy(properties);
        ExchangeTranslateCacheStrategy exchangeStrategy = new ExchangeTranslateCacheStrategy(properties);
        TranslateCacheStrategyRegistry registry = new TranslateCacheStrategyRegistry(
                List.of(dictStrategy, exchangeStrategy, new NoCacheStrategy()),
                dictStrategy, exchangeStrategy);

        cacheManager = new TranslateCacheManager(
                registry,
                List.of(new InMemoryTranslateCacheStore()),
                properties);

        translateCount = new AtomicInteger(0);
    }

    @Test
    @DisplayName("相同 key 第二次翻译应命中缓存")
    void shouldHitCacheOnSecondCall() throws Exception {
        FieldTranslator translator = (fieldValue, context) -> {
            translateCount.incrementAndGet();
            return "已支付";
        };

        TranslateContext context = new TranslateContext();
        Field field = Dummy.class.getDeclaredField("status");

        Object first = cacheManager.translateWithCache(
                translator, DictFieldTranslator.class, DefaultTranslateCacheStrategy.class,
                1, context, new String[]{"order_status"}, field);
        Object second = cacheManager.translateWithCache(
                translator, DictFieldTranslator.class, DefaultTranslateCacheStrategy.class,
                1, context, new String[]{"order_status"}, field);

        assertThat(first).isEqualTo("已支付");
        assertThat(second).isEqualTo("已支付");
        assertThat(translateCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("NoCacheStrategy 应禁用缓存")
    void shouldSkipCacheWhenNoCacheStrategy() throws Exception {
        FieldTranslator translator = (fieldValue, context) -> {
            translateCount.incrementAndGet();
            return "value";
        };

        TranslateContext context = new TranslateContext();
        Field field = Dummy.class.getDeclaredField("status");

        cacheManager.translateWithCache(
                translator, DictFieldTranslator.class, NoCacheStrategy.class,
                1, context, new String[]{"order_status"}, field);
        cacheManager.translateWithCache(
                translator, DictFieldTranslator.class, NoCacheStrategy.class,
                1, context, new String[]{"order_status"}, field);

        assertThat(translateCount.get()).isEqualTo(2);
    }

    static class Dummy {
        Integer status;
    }
}
