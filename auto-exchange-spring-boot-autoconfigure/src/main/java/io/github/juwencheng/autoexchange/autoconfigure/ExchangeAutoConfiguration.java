package io.github.juwencheng.autoexchange.autoconfigure;

import io.github.juwencheng.autoexchange.autoconfigure.exception.ExchangeExceptionHandler;
import io.github.juwencheng.autoexchange.autoconfigure.validation.RateRefreshConfigurationValidator;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;
import io.github.juwencheng.autoexchange.exchange.ExchangeContextContributor;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.exchange.ExchangeProperties;
import io.github.juwencheng.autoexchange.exchange.cache.ExchangeTranslateCacheStrategy;
import io.github.juwencheng.autoexchange.provider.DefaultExchangeDataProvider;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.scheduler.DynamicRateRefreshScheduler;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateStrategy;
import io.github.juwencheng.fieldtranslate.core.translate.cache.TranslatorCacheBinding;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RestController;

/**
 * 汇率转换插件自动配置。
 *
 * @author juwencheng
 */
@AutoConfiguration
@AutoConfigureAfter(name = "io.github.juwencheng.fieldtranslate.autoconfigure.FieldTranslateAutoConfiguration")
@ConditionalOnWebApplication
@ConditionalOnClass(RestController.class)
@ConditionalOnBean(TranslateStrategy.class)
@ConditionalOnProperty(prefix = "auto.exchange", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(ExchangeProperties.class)
public class ExchangeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IExchangeDataProvider exchangeDataProvider() {
        return new DefaultExchangeDataProvider();
    }

    @Bean
    public ExchangeManager exchangeManager(ExchangeProperties exchangeProperties,
                                           IExchangeDataProvider dataProvider) {
        return new ExchangeManager(dataProvider, exchangeProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExchangeFieldTranslator exchangeFieldTranslator(ExchangeManager exchangeManager,
                                                         ExchangeProperties properties) {
        return new ExchangeFieldTranslator(exchangeManager, properties);
    }

    @Bean
    public ExchangeContextContributor exchangeContextContributor(ExchangeProperties properties) {
        return new ExchangeContextContributor(properties);
    }

    @Bean
    public ExchangeTranslateCacheStrategy exchangeTranslateCacheStrategy(ExchangeProperties properties) {
        return new ExchangeTranslateCacheStrategy(properties);
    }

    @Bean
    public TranslatorCacheBinding exchangeTranslatorCacheBinding() {
        return new TranslatorCacheBinding(ExchangeFieldTranslator.class, ExchangeTranslateCacheStrategy.class);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ExchangeExceptionHandler exchangeExceptionHandler() {
        return new ExchangeExceptionHandler();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auto.exchange.rate-refresh", name = "enabled", havingValue = "true")
    static class RateRefreshSchedulingConfiguration {

        @Bean
        public RateRefreshConfigurationValidator rateRefreshConfigurationValidator(ApplicationContext context,
                                                                                 ExchangeProperties properties) {
            return new RateRefreshConfigurationValidator(context, properties);
        }

        @Bean
        @ConditionalOnBean(TaskScheduler.class)
        public DynamicRateRefreshScheduler dynamicRateRefreshScheduler(TaskScheduler taskScheduler,
                                                                       ExchangeManager exchangeManager,
                                                                       ExchangeProperties properties) {
            return new DynamicRateRefreshScheduler(taskScheduler, exchangeManager, properties);
        }
    }
}
