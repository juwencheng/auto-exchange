package io.github.juwencheng.autoexchange.core.enums;

/**
 * 汇率的两种模式
 *
 */
public enum ApplyExchangeMode {
    // 在VO中实现接口{@see IApplyExchange}方法，然后在applyExchange方法中调用
    INPLACE,
    APPEND
}
