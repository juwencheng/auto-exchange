package tech.baizi.autoexchange.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated // 启用JSR-303校验
@ConfigurationProperties(prefix = "auto.exchange")
public class AutoExchangeProperties {

    // 启动时刷新数据
    private boolean refreshOnLaunch = false;

    private RateRefresh rateRefresh = new RateRefresh();

    public RateRefresh getRateRefresh() {
        return rateRefresh;
    }

    public void setRateRefresh(RateRefresh rateRefresh) {
        this.rateRefresh = rateRefresh;
    }

    public boolean isRefreshOnLaunch() {
        return refreshOnLaunch;
    }

    public void setRefreshOnLaunch(boolean refreshOnLaunch) {
        this.refreshOnLaunch = refreshOnLaunch;
    }

    public static class RateRefresh {
        // 是否启动自动刷新
        private boolean enabled = false;


        @NotEmpty(message = "当启用汇率刷新时，CRON表达式不能为空")
        private String cron;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }


    }
}
