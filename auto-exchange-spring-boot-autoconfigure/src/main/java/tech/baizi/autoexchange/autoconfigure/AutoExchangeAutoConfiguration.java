package tech.baizi.autoexchange.autoconfigure;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RestController;
import tech.baizi.autoexchange.aspect.AutoExchangeAspect;
import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.manager.ExchangeManager;
import tech.baizi.autoexchange.core.strategy.AutoApplyExchangeStrategy;
import tech.baizi.autoexchange.core.support.TypedResultWrapper;
import tech.baizi.autoexchange.core.support.TypedResultWrapperSerializer;
import tech.baizi.autoexchange.provider.DefaultExchangeDataProvider;
import tech.baizi.autoexchange.provider.IExchangeDataProvider;
import tech.baizi.autoexchange.core.strategy.AppendApplyExchangeStrategy;
import tech.baizi.autoexchange.core.strategy.IApplyExchangeStrategy;
import tech.baizi.autoexchange.core.strategy.InPlaceApplyExchangeStrategy;
import tech.baizi.autoexchange.scheduler.DynamicRateRefreshScheduler;
import tech.baizi.autoexchange.service.CurrencyExchangeService;

@Configuration
// 只有在web程序中生效
@ConditionalOnWebApplication
@ConditionalOnClass({RestController.class})
@EnableConfigurationProperties({AutoExchangeProperties.class})
public class AutoExchangeAutoConfiguration {

    @Bean
    public ApplicationRunner autoExchangeApplicationRunner() {
        return args -> {
            System.out.println("autoExchangeApplicationRunner");
        };
    }

    // ------------- 注册应用汇率的策略方法类 ------
    @Bean
    @ConditionalOnMissingBean
    public IApplyExchangeStrategy autoExchangeStrategy() {
        return new AutoApplyExchangeStrategy(new InPlaceApplyExchangeStrategy(), new AppendApplyExchangeStrategy());
    }

    @Bean
    public AutoExchangeAspect autoExchangeAspect(IApplyExchangeStrategy applyExchangeStrategy) {
        return new AutoExchangeAspect(applyExchangeStrategy);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exchangeWrapperSerializerCustomizer() {
        return builder -> builder.serializerByType(TypedResultWrapper.class, new TypedResultWrapperSerializer());
    }

    @Bean
    @ConditionalOnMissingBean(IExchangeDataProvider.class)
    public IExchangeDataProvider exchangeDataProvider() {
        return new DefaultExchangeDataProvider();
    }

    @Bean
    public ExchangeManager exchangeManager(AutoExchangeProperties autoExchangeProperties, IExchangeDataProvider dataProvider) {
        return new ExchangeManager(new CurrencyExchangeService(dataProvider), autoExchangeProperties);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auto.exchange.rate-refresh", name = "enabled", havingValue = "true")
    public static class RateRefreshSchedulingConfiguration {
        @Bean
        public DynamicRateRefreshScheduler dynamicRateRefreshScheduler(TaskScheduler taskScheduler, ExchangeManager exchangeManager, AutoExchangeProperties properties) {
            return new DynamicRateRefreshScheduler(taskScheduler, exchangeManager, properties);
        }
    }

}
