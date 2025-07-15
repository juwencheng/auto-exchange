package io.github.juwencheng.autoexchange.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.strategy.IApplyExchangeStrategy;

// 设置一个非常低的优先级（即一个非常大的数值）
// 这能确保我们的切面在大多数其他切面（如@Transactional, @Cacheable）之后执行
@Aspect
public class AutoExchangeAspect implements Ordered {
    private final IApplyExchangeStrategy applyExchangeStrategy;
    private final AutoExchangeProperties properties;

    public AutoExchangeAspect(IApplyExchangeStrategy applyExchangeStrategy, AutoExchangeProperties properties) {
        this.applyExchangeStrategy = applyExchangeStrategy;
        this.properties = properties;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {

    }

    @Pointcut("@annotation(io.github.juwencheng.autoexchange.core.annotation.AutoExchangeResponse)")
    public void autoExchangeResponse() {

    }

    //    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && @annotation(tech.baizi.autoexchange.core.annotation.AutoExchangeResponse)")
    public void autoExchangeEnableMethods() {

    }

    @Around("autoExchangeResponse()")
    public Object handleExchange(ProceedingJoinPoint joinPoint) throws Throwable {
        Object originalResult = joinPoint.proceed();
        if (originalResult == null) {
            return null;
        }
        applyExchangeStrategy.applyExchange(originalResult);
        return originalResult;
    }

    @Override
    public int getOrder() {
        return this.properties.getAspectOrder();
    }
}
