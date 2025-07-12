package tech.baizi.autoexchange.core.strategy;


import tech.baizi.autoexchange.core.AutoExchangeProperties;
import tech.baizi.autoexchange.core.strategy.meta.ClassMetadata;

import java.util.*;

public class AutoApplyExchangeStrategy extends AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    private final InPlaceApplyExchangeStrategy inPlaceApplyExchangeStrategy;
    private final AppendApplyExchangeStrategy appendApplyExchangeStrategy;

    public AutoApplyExchangeStrategy(InPlaceApplyExchangeStrategy inPlaceApplyExchangeStrategy, AppendApplyExchangeStrategy appendApplyExchangeStrategy, AutoExchangeProperties properties) {
        super(properties);
        this.inPlaceApplyExchangeStrategy = inPlaceApplyExchangeStrategy;
        this.appendApplyExchangeStrategy = appendApplyExchangeStrategy;
    }

    @Override
    protected Object createPlaceholder(Object object) {
        if (object instanceof Collection) {
            return new LinkedList<>();
        } else if (object instanceof Map) {
            return new HashMap<>(((Map<?, ?>) object).size());
        }
        ClassMetadata classMetadata = getClassMetadata(object.getClass());

        if (classMetadata == null) {
            classMetadata = buildClassMetadata(object.getClass());
        };
        if (classMetadata.isApplyExchangeImplementor()) {
            return object;
        }
        return new LinkedHashMap<>();
    }

    @Override
    protected Object decideNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged) {
        ClassMetadata classMetadata = getClassMetadata(originalObject.getClass());
        if (classMetadata == null) return originalObject;
        boolean isApplyImplementor = classMetadata.isApplyExchangeImplementor();

        if (isApplyImplementor) {
            if (hasChildrenChanged) {
                throw new IllegalStateException("对象 " + originalObject.getClass().getName() +
                        " 实现了IApplyExchange (In-place模式), 但其子节点被转换为了Map (Append模式), 这是不被支持的混合用法。");
            }
            return inPlaceApplyExchangeStrategy.decideNodeTransformation(originalObject, processedProperties, hasChildrenChanged);
        }
        return appendApplyExchangeStrategy.decideNodeTransformation(originalObject, processedProperties, hasChildrenChanged);
    }
}
