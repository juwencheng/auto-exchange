package tech.baizi.autoexchange.core.strategy;

import tech.baizi.autoexchange.core.IApplyExchange;
import tech.baizi.autoexchange.core.strategy.meta.ClassMetadata;

import java.math.BigDecimal;
import java.util.Map;

public class InPlaceApplyExchangeStrategy extends AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    @Override
    protected Object decidedNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged) {
        // Inplace模式下，子节点永远不会改变类型，所以isPropertyConvert总是false
        // 我们只关心当前节点是否需要原地修改
        ClassMetadata classMetadata = getClassMetadata(originalObject.getClass());
        if (classMetadata.isApplyExchangeImplementor()) {
            // ... 执行原地修改的逻辑 ...
            ((IApplyExchange) originalObject).applyExchange("CNY", BigDecimal.valueOf(2));
        }
        // Inplace模式永远返回原始对象
        return originalObject;
    }
}
