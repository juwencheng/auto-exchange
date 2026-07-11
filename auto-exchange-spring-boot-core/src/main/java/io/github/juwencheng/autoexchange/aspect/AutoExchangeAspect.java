package io.github.juwencheng.autoexchange.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.strategy.IApplyExchangeStrategy;
import io.github.juwencheng.autoexchange.core.translate.TranslateStrategy;

/**
 * 汇率转换 AOP 切面。拦截标注了 @AutoExchangeResponse 的方法，
 * 在方法返回后执行汇率转换策略和通用翻译策略。
 * <p>
 * @AutoExchangeResponse 同时触发：
 * 1. 旧有的汇率转换逻辑（IApplyExchangeStrategy）
 * 2. 通用翻译框架（TranslateStrategy，处理 @TranslateField 标注的字段）
 *
 * @author juwencheng
 */
@Aspect
public class AutoExchangeAspect implements Ordered {
    private final IApplyExchangeStrategy applyExchangeStrategy;
    private final AutoExchangeProperties properties;
    private TranslateStrategy translateStrategy;

    public AutoExchangeAspect(IApplyExchangeStrategy applyExchangeStrategy, AutoExchangeProperties properties) {
        this.applyExchangeStrategy = applyExchangeStrategy;
        this.properties = properties;
    }

    public void setTranslateStrategy(TranslateStrategy translateStrategy) {
        this.translateStrategy = translateStrategy;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {
    }

    @Pointcut("@annotation(io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse)")
    public void autoExchangeResponse() {
    }

    @Around("autoExchangeResponse()")
    public Object handleExchange(ProceedingJoinPoint joinPoint) throws Throwable {
        Object originalResult = joinPoint.proceed();
        if (originalResult == null) {
            return null;
        }
        applyExchangeStrategy.applyExchange(originalResult);
        if (translateStrategy != null) {
            translateStrategy.applyTranslation(originalResult);
        }
        return originalResult;
    }

    @Override
    public int getOrder() {
        return this.properties.getAspectOrder();
    }
}
