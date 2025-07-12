package tech.baizi.autoexchange.core.enums;

/**
 * 缺失汇率处理策略的枚举
 */
public enum MissingRateStrategy {

    /**
     * 抛出异常
     */
    THROW_EXCEPTION,
    /**
     * 返回一个预定义的保护性汇率值
     */
    PROTECTIVE,
    /**
     * 返回null，或者optional.empty()，由调用方负责处理
     */
    RETURN_NULL
}
