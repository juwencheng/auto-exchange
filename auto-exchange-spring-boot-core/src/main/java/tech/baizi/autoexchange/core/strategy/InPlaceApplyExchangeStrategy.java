package tech.baizi.autoexchange.core.strategy;

import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.IApplyExchange;
import tech.baizi.autoexchange.core.strategy.meta.ClassMetadata;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * 弃用了，主要是APPEND模式下无法正常使用，导致INPLACE模式也重构了，见{@link tech.baizi.autoexchange.core.strategy.AutoApplyExchangeStrategy}
 */
@Deprecated
public class InPlaceApplyExchangeStrategy extends AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    public InPlaceApplyExchangeStrategy(AutoExchangeProperties properties) {
        super(properties);
    }

    @Override
    protected Object createPlaceholder(Object object) {
        return object;
    }

    @Override
    protected Object decideNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged) {
        // Inplace模式下，子节点永远不会改变类型，所以isPropertyConvert总是false
        // 我们只关心当前节点是否需要原地修改
        ClassMetadata classMetadata = getClassMetadata(originalObject.getClass());
        if (classMetadata != null && classMetadata.isApplyExchangeImplementor()) {
            String baseCurrency = resolveBaseCurrency(originalObject, classMetadata);
            // ... 执行原地修改的逻辑 ...
            ((IApplyExchange) originalObject).applyExchange("CNY", Optional.ofNullable(BigDecimal.valueOf(2)));
        }
        // Inplace模式永远返回原始对象
        return originalObject;
    }
}
