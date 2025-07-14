package io.github.juwencheng.autoexchange.autoconfigure.validation;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.exception.ExchangeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;

public class RateRefreshConfigurationValidator implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RateRefreshConfigurationValidator.class);
    private final ApplicationContext applicationContext;
    private final AutoExchangeProperties properties;

    public RateRefreshConfigurationValidator(ApplicationContext applicationContext, AutoExchangeProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 确保只在根应用上下文刷新时执行一次
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        if (properties.getRateRefresh().isEnabled()) {
            try {
                TaskScheduler bean = applicationContext.getBean(TaskScheduler.class);
                logger.debug("找到了TaskScheduler，开启汇率自动刷新功能");
            } catch (BeansException e) {
                // 如果找不到Bean，就抛出我们自定义的、信息明确的异常
                throw new ExchangeConfigurationException(
                        "自动汇率刷新功能已启用 (auto.exchange.rate-refresh.enabled=true), " +
                                "但未找到 TaskScheduler Bean。请在您的主应用或任何一个@Configuration类上添加 @EnableScheduling 注解。"
                );
            }
        }
    }
}
