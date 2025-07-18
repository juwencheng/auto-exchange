package io.github.juwencheng.autoexchange.core.strategy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.IApplyExchange;
import io.github.juwencheng.autoexchange.core.annotation.AutoExchangeField;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContext;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContextHolder;
import io.github.juwencheng.autoexchange.core.dto.ExchangeInfoRateDto;
import io.github.juwencheng.autoexchange.core.dto.ExchangeResultDto;
import io.github.juwencheng.autoexchange.core.manager.ExchangeManager;
import io.github.juwencheng.autoexchange.core.strategy.meta.ClassMetadata;
import io.github.juwencheng.autoexchange.core.tools.BigDecimalTools;
import io.github.juwencheng.autoexchange.exception.ExchangeConfigurationException;
import io.github.juwencheng.autoexchange.exception.ExchangeProcessingException;
import io.github.juwencheng.autoexchange.exception.ExchangeRateNotFoundException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

public class AutoApplyExchangeStrategy extends AbstractApplyExchangeStrategy implements IApplyExchangeStrategy {
    private static final Logger log = LoggerFactory.getLogger(AutoApplyExchangeStrategy.class);
    private final ExchangeManager exchangeManager;

    public AutoApplyExchangeStrategy(AutoExchangeProperties properties, ExchangeManager exchangeManager) {
        super(properties);
        this.exchangeManager = exchangeManager;
    }

    @Override
    protected Object createPlaceholder(Object object) {
        return object;
    }

    @Override
    protected Object decideNodeTransformation(Object originalObject, Map<String, Object> processedProperties, boolean hasChildrenChanged) {
        return null;
    }

    @Override
    public Object applyExchange(Object rootObject) {
        if (rootObject == null) return rootObject;
        // 使用递归创建整个对象图
        traverseObjectGraph(rootObject, new IdentityHashMap<>());
        return rootObject;
    }

    private void traverseObjectGraph(Object object, Map<Object, Object> visitedMap) {
        if (object == null || visitedMap.containsKey(object)) {
            return;
        }
        visitedMap.put(object, Boolean.TRUE);
        // --- 核心处理逻辑 ---
        ClassMetadata metadata = getClassMetadata(object.getClass());
        AutoExchangeContext context = AutoExchangeContextHolder.getContext();
        String targetCurrency = context.getTargetCurrency();
        String baseCurrency = resolveBaseCurrency(object, metadata);
        Optional<ExchangeInfoRateDto> rate = exchangeManager.getRate(baseCurrency, targetCurrency);

        // 1. 先append
        for (Field exchangeableField : metadata.getExchangeableFields()) {
            boolean canAccess = exchangeableField.canAccess(object);
            try {
                if (!canAccess) {
                    exchangeableField.setAccessible(true);
                }
                Object fieldValue = exchangeableField.get(object);
                ExchangeResultDto exchangeResult = rate.map(exchangeInfoRateDto -> new ExchangeResultDto(exchangeInfoRateDto, BigDecimalTools.multiply(BigDecimalTools.convertOrDefault(fieldValue, BigDecimal.ZERO), exchangeInfoRateDto.getRate())))
                        .orElseGet(() -> resolveMissRate(fieldValue, baseCurrency, targetCurrency));
                String newFieldName = exchangeableField.getAnnotation(AutoExchangeField.class).value();
                if (newFieldName == null || newFieldName.trim().isEmpty()) {
                    newFieldName = exchangeableField.getName() + "AutoExchange";
                }
                context.addAppendedData(object, newFieldName, exchangeResult.toMap());
            } catch (IllegalAccessException e) {
                log.error("对@AutoExchangeField注解标注的属性进行自动换汇失败：" + object.getClass() + "." + exchangeableField.getName(), e);
                throw new ExchangeProcessingException("对@AutoExchangeField注解标注的属性进行自动换汇失败：" + object.getClass() + "." + exchangeableField.getName(), e);
            } finally {
                // 恢复原始状态
                if (!canAccess) {
                    exchangeableField.setAccessible(false);
                }
            }
        }
        // 2. 再In-place ，避免重复计算
        if (metadata.isApplyExchangeImplementor()) {
            ExchangeInfoRateDto exchangeInfoRateDto = rate.orElseGet(() -> resolveMissRate(object, baseCurrency, targetCurrency));
            ((IApplyExchange) object).applyExchange(targetCurrency, Optional.ofNullable(exchangeInfoRateDto.getRate()));
        }

        // --- 递归遍历子节点 ---
        if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> traverseObjectGraph(item, visitedMap));
        } else if (object instanceof Map) {
            ((Map<?, ?>) object).values().forEach(item -> traverseObjectGraph(item, visitedMap));
        } else {
            for (PropertyDescriptor pd : metadata.getPropertiesToInspect()) {
                try {
                    Object propertyValue = pd.getReadMethod().invoke(object);
                    traverseObjectGraph(propertyValue, visitedMap);
                } catch (Exception e) {
                    throw new ExchangeProcessingException("解析对象的属性对象图失败", e);
                }
            }
        }
    }

    private ExchangeResultDto resolveMissRate(Object fieldValue, String baseCurrency, String targetCurrency) {
        AutoExchangeProperties.MissingRate missingRate = this.properties.getMissingRate();
        switch (missingRate.getMissingRateStrategy()) {
            case THROW_EXCEPTION:
                throw new ExchangeRateNotFoundException(baseCurrency, targetCurrency);
            case PROTECTIVE:
                BigDecimal decimal = BigDecimalTools.multiply(BigDecimalTools.convertOrDefault(fieldValue, BigDecimal.ZERO), missingRate.getProtectiveRateValue());
                return new ExchangeResultDto(new ExchangeInfoRateDto(baseCurrency, targetCurrency, missingRate.getProtectiveRateValue()), decimal);
            case RETURN_NULL:
                return new ExchangeResultDto(new ExchangeInfoRateDto(baseCurrency, targetCurrency, null), null);
        }
        throw new ExchangeConfigurationException("不支持的缺失汇率策略");
    }
}
