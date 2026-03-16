package io.github.juwencheng.autoexchange.core.enums;

/**
 * 汇率的两种模式
 *
 * @deprecated 此枚举未被框架使用，保留仅供参考
 */
@Deprecated
public enum ApplyExchangeMode {
    // 在VO中实现接口{@see IApplyExchange}方法，然后在applyExchange方法中调用
    INPLACE,
    APPEND
}
