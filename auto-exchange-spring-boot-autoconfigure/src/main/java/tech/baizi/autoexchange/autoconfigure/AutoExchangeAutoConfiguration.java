package tech.baizi.autoexchange.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import tech.baizi.autoexchange.core.datasource.IDataProvider;
import tech.baizi.autoexchange.core.strategy.AppendApplyExchangeStrategy;
import tech.baizi.autoexchange.core.strategy.IApplyExchangeStrategy;
import tech.baizi.autoexchange.core.strategy.InPlaceApplyExchangeStrategy;

@Configuration
// 只有在web程序中生效
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "auto.exchange", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnClass({RestController.class, ObjectMapper.class})
@EnableConfigurationProperties({AutoExchangeProperties.class})
public class AutoExchangeAutoConfiguration {

    /**
     * 处理ObjectMapper的Bean
     * <p>
     * 如果用户的Spring上下文中已经存在一个ObjectMapper的Bean，此方法将不会被执行。
     * 如果不存在，Spring将调用此方法创建一个默认的ObjectMapper Bean，并放入上下文中。
     * 这样，我们后续的Aspect就能确保总有一个ObjectMapper可用。
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    @ConditionalOnMissingBean(name = "autoExchangeDataProvider")
    public IDataProvider autoExchangeDataProvider() {
        return null;
    }

    @Bean
    public ApplicationRunner autoExchangeApplicationRunner() {
        return args -> {
            System.out.println("autoExchangeApplicationRunner");
        };
    }

    // ------------- 注册应用汇率的策略方法类 ------

    @Bean
    @ConditionalOnProperty(prefix = "auto.exchange", name = "mode", havingValue = "INPLACE", matchIfMissing = false)
    public IApplyExchangeStrategy inPlaceApplyExchangeStrategy() {
        return new InPlaceApplyExchangeStrategy();
    }

    @Bean
    @ConditionalOnProperty(prefix = "auto.exchange", name = "mode", havingValue = "APPEND", matchIfMissing = false)
    public IApplyExchangeStrategy applyExchangeStrategy() {
        return new AppendApplyExchangeStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public IApplyExchangeStrategy autoExchangeStrategy() {
        return new InPlaceApplyExchangeStrategy();
    }
}
