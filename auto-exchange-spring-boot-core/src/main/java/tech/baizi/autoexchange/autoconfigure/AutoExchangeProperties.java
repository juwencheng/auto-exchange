package tech.baizi.autoexchange.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tech.baizi.autoexchange.core.enums.ApplyExchangeMode;

@ConfigurationProperties(prefix = "auto.exchange")
public class AutoExchangeProperties {
    /**
     * 是否开启
     */
    private boolean enabled = true;
    /**
     * 更新的定时任务
     */
    private String cron = "";

    /**
     * 应用模式
     * INPLACE: 直接替换
     * APPEND: 追加新的属性，但是需要将对象转成map，可能导致其他的切面读取到的不是原始对象，而是map，对系统造成侵入
     */
    private ApplyExchangeMode mode = ApplyExchangeMode.INPLACE;

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

    public ApplyExchangeMode getMode() {
        return mode;
    }

    public void setMode(ApplyExchangeMode mode) {
        this.mode = mode;
    }
}
