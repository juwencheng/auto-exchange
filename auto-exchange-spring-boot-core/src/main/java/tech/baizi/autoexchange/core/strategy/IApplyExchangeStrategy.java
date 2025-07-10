package tech.baizi.autoexchange.core.strategy;

/**
 * 应用汇率的策略
 */
public interface IApplyExchangeStrategy {

    /**
     * 在对象上应用汇率转换
     *
     * @param object 对象
     * @return 应用后的对象
     */
    Object applyExchange(Object object);
}
