package io.github.juwencheng.autoexchange.autoconfigure;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RestController;
import io.github.juwencheng.autoexchange.aspect.AutoExchangeAspect;
import io.github.juwencheng.autoexchange.autoconfigure.exception.AutoExchangeExceptionHandler;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;
import io.github.juwencheng.autoexchange.core.serialize.ExchangeBeanSerializerModifier;
import io.github.juwencheng.autoexchange.core.strategy.AutoApplyExchangeStrategy;
import io.github.juwencheng.autoexchange.core.strategy.IApplyExchangeStrategy;
import io.github.juwencheng.autoexchange.provider.DefaultExchangeDataProvider;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.scheduler.DynamicRateRefreshScheduler;

@Configuration
// 只有在web程序中生效
@ConditionalOnWebApplication
@ConditionalOnClass({RestController.class})
@EnableConfigurationProperties({AutoExchangeProperties.class})
public class AutoExchangeAutoConfiguration {

//    @Bean
//    public ApplicationRunner autoExchangeApplicationRunner() {
//        return args -> {
//            System.out.println("autoExchangeApplicationRunner");
//        };
//    }

    // ------------- 注册应用汇率的策略方法类 ------

    @Bean
    @ConditionalOnMissingBean
    public AutoApplyExchangeStrategy autoExchangeStrategy(AutoExchangeProperties properties, ExchangeManager exchangeManager) {
        return new AutoApplyExchangeStrategy(properties, exchangeManager);
    }

    @Bean
    public AutoExchangeAspect autoExchangeAspect(IApplyExchangeStrategy autoApplyExchangeStrategy, AutoExchangeProperties properties) {
        return new AutoExchangeAspect(autoApplyExchangeStrategy, properties);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer exchangeWrapperSerializerCustomizer() {
        return builder -> {
            builder.postConfigurer(objectMapper -> {
                // ObjectMapper的SerializerFactory是负责创建和缓存序列化器的组件。
                // 我们需要获取它，并给它加上我们的Modifier。
                // withSerializerModifier()会返回一个新的Factory实例，所以我们需要set回去。
                objectMapper.setSerializerFactory(
                        objectMapper.getSerializerFactory()
                                .withSerializerModifier(new ExchangeBeanSerializerModifier())
                );
            });
        };
    }

    @Bean
    @ConditionalOnMissingBean(IExchangeDataProvider.class)
    public IExchangeDataProvider exchangeDataProvider() {
        return new DefaultExchangeDataProvider();
    }

    @Bean
    public ExchangeManager exchangeManager(AutoExchangeProperties autoExchangeProperties, IExchangeDataProvider dataProvider) {
        return new ExchangeManager(dataProvider, autoExchangeProperties);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public AutoExchangeExceptionHandler exchangeExceptionHandler() {
        return new AutoExchangeExceptionHandler();
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
