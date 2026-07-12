package io.github.juwencheng.autoexchange.autoconfigure;

import io.github.juwencheng.autoexchange.autoconfigure.validation.RateRefreshConfigurationValidator;
import io.github.juwencheng.autoexchange.core.interceptor.AutoExchangeInterceptor;
import io.github.juwencheng.autoexchange.core.translate.*;
import io.github.juwencheng.autoexchange.core.translate.cache.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RestController;
import io.github.juwencheng.autoexchange.aspect.AutoExchangeAspect;
import io.github.juwencheng.autoexchange.autoconfigure.exception.AutoExchangeExceptionHandler;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;
import io.github.juwencheng.autoexchange.core.strategy.AutoApplyExchangeStrategy;
import io.github.juwencheng.autoexchange.core.strategy.IApplyExchangeStrategy;
import io.github.juwencheng.autoexchange.provider.DefaultExchangeDataProvider;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.scheduler.DynamicRateRefreshScheduler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({RestController.class})
@EnableConfigurationProperties({AutoExchangeProperties.class})
public class AutoExchangeAutoConfiguration {

    // ==================== 旧有汇率转换组件（向后兼容） ====================

    @Bean
    @ConditionalOnMissingBean
    public IApplyExchangeStrategy autoExchangeStrategy(AutoExchangeProperties properties, ExchangeManager exchangeManager) {
        return new AutoApplyExchangeStrategy(properties, exchangeManager);
    }

    @Bean
    public AutoExchangeAspect autoExchangeAspect(IApplyExchangeStrategy autoApplyExchangeStrategy, AutoExchangeProperties properties, TranslateStrategy translateStrategy) {
        AutoExchangeAspect aspect = new AutoExchangeAspect(autoApplyExchangeStrategy, properties);
        aspect.setTranslateStrategy(translateStrategy);
        return aspect;
    }

    @Bean
    public AutoExchangeInterceptor exchangeContextInterceptor(AutoExchangeProperties properties) {
        return new AutoExchangeInterceptor(properties);
    }

    // ==================== 通用翻译框架组件 ====================

    @Bean
    @ConditionalOnMissingBean
    public ExchangeFieldTranslator exchangeFieldTranslator(ExchangeManager exchangeManager, AutoExchangeProperties properties) {
        return new ExchangeFieldTranslator(exchangeManager, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public IDictDataProvider dictDataProvider() {
        return new DefaultDictDataProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public DictFieldTranslator dictFieldTranslator(IDictDataProvider dictDataProvider) {
        return new DictFieldTranslator(dictDataProvider);
    }

    @Bean
    public ExchangeContextContributor exchangeContextContributor(AutoExchangeProperties properties) {
        return new ExchangeContextContributor(properties);
    }

    // ==================== 翻译缓存组件 ====================

    @Bean
    @ConditionalOnMissingBean
    public InMemoryTranslateCacheStore inMemoryTranslateCacheStore() {
        return new InMemoryTranslateCacheStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public DictTranslateCacheStrategy dictTranslateCacheStrategy(AutoExchangeProperties properties) {
        return new DictTranslateCacheStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExchangeTranslateCacheStrategy exchangeTranslateCacheStrategy(AutoExchangeProperties properties) {
        return new ExchangeTranslateCacheStrategy(properties);
    }

    @Bean
    public TranslateCacheStrategyRegistry translateCacheStrategyRegistry(
            List<TranslateCacheStrategy> strategies,
            DictTranslateCacheStrategy dictStrategy,
            ExchangeTranslateCacheStrategy exchangeStrategy) {
        return new TranslateCacheStrategyRegistry(strategies, dictStrategy, exchangeStrategy);
    }

    @Bean
    public TranslateCacheManager translateCacheManager(
            TranslateCacheStrategyRegistry strategyRegistry,
            List<TranslateCacheStore> stores,
            AutoExchangeProperties properties) {
        return new TranslateCacheManager(strategyRegistry, stores, properties);
    }

    @Bean
    public TranslateStrategy translateStrategy(List<FieldTranslator> translators, TranslateCacheManager cacheManager) {
        return new TranslateStrategy(translators, cacheManager);
    }

    @Bean
    public TranslateAspect translateAspect(TranslateStrategy translateStrategy, AutoExchangeProperties properties) {
        return new TranslateAspect(translateStrategy, properties.getAspectOrder() - 1);
    }

    @Bean
    public TranslateInterceptor translateInterceptor(List<TranslateContextContributor> contributors) {
        return new TranslateInterceptor(contributors);
    }

    /**
     * 统一的 Jackson BeanSerializerModifier，同时读取 AutoExchangeContext 和 TranslateContext
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exchangeWrapperSerializerCustomizer() {
        return builder -> {
            builder.postConfigurer(objectMapper -> {
                objectMapper.setSerializerFactory(
                        objectMapper.getSerializerFactory()
                                .withSerializerModifier(new TranslateBeanSerializerModifier())
                );
            });
        };
    }

    // ==================== 通用组件 ====================

    @Bean
    @ConditionalOnMissingBean
    public IExchangeDataProvider exchangeDataProvider() {
        return new DefaultExchangeDataProvider();
    }

    @Bean
    public ExchangeManager exchangeManager(AutoExchangeProperties autoExchangeProperties, IExchangeDataProvider dataProvider) {
        return new ExchangeManager(dataProvider, autoExchangeProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public AutoExchangeExceptionHandler exchangeExceptionHandler() {
        return new AutoExchangeExceptionHandler();
    }

    @Bean
    public WebMvcConfigurer exchangeInterceptorConfigurer(
            AutoExchangeInterceptor exchangeInterceptor,
            TranslateInterceptor translateInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(exchangeInterceptor);
                registry.addInterceptor(translateInterceptor);
            }
        };
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auto.exchange.rate-refresh", name = "enabled", havingValue = "true")
    public static class RateRefreshSchedulingConfiguration {

        @Bean
        public RateRefreshConfigurationValidator rateRefreshConfigurationValidator(ApplicationContext context, AutoExchangeProperties properties) {
            return new RateRefreshConfigurationValidator(context, properties);
        }

        @Bean
        @ConditionalOnBean(TaskScheduler.class)
        public DynamicRateRefreshScheduler dynamicRateRefreshScheduler(TaskScheduler taskScheduler, ExchangeManager exchangeManager, AutoExchangeProperties properties) {
            return new DynamicRateRefreshScheduler(taskScheduler, exchangeManager, properties);
        }
    }
}
