package io.github.juwencheng.autoexchange.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;

import java.util.TimeZone;

public class DynamicRateRefreshScheduler implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger log = LoggerFactory.getLogger(DynamicRateRefreshScheduler.class);
    private final TaskScheduler taskScheduler;
    private final ExchangeManager exchangeManager;
    private final AutoExchangeProperties properties;

    public DynamicRateRefreshScheduler(TaskScheduler taskScheduler, ExchangeManager exchangeManager, AutoExchangeProperties properties) {
        this.taskScheduler = taskScheduler;
        this.exchangeManager = exchangeManager;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        AutoExchangeProperties.RateRefresh refreshConfig = properties.getRateRefresh();

        // 再次确认是否启用 (虽然Bean的创建已经有条件判断，但这里更明确)
        if (refreshConfig.isEnabled()) {
            String cron = refreshConfig.getCron();
            log.info("检测到汇率刷新功能已启用。准备安排定时任务...");

            try {
                // 使用CronTrigger来解析表达式，它会在表达式无效时抛出异常
                CronTrigger trigger = new CronTrigger(cron, TimeZone.getDefault());

                // 定义需要执行的任务，即调用service的方法
                Runnable task = () -> exchangeManager.refreshRates();

                // 使用TaskScheduler来安排任务
                taskScheduler.schedule(task, trigger);
                log.info("成功安排汇率刷新任务。CRON表达式: [{}], 时区: {}", cron, TimeZone.getDefault().getID());
            } catch (IllegalArgumentException e) {
                log.error("无效的CRON表达式 [{}]: {}. 汇率刷新任务无法启动。", cron, e.getMessage());
            }
        }
    }
}
