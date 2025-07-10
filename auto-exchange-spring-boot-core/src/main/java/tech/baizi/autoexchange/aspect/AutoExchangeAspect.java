package tech.baizi.autoexchange.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
import tech.baizi.autoexchange.core.strategy.IApplyExchangeStrategy;
import tech.baizi.autoexchange.core.support.TypedResultWrapper;

// 关键：设置一个非常低的优先级（即一个非常大的数值）
// 这能确保我们的切面在大多数其他切面（如@Transactional, @Cacheable）之后执行
// 从而避免我们改变返回类型后，影响到期望接收原始DTO的其他切面。
@Aspect
//@Order(Ordered.LOWEST_PRECEDENCE)
public class AutoExchangeAspect {

    private final IApplyExchangeStrategy applyExchangeStrategy;

    public AutoExchangeAspect(IApplyExchangeStrategy applyExchangeStrategy) {
        this.applyExchangeStrategy = applyExchangeStrategy;
    }


    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && @annotation(tech.baizi.autoexchange.core.annotation.EnableAutoExchange)")
    public void autoExchangeEnableMethods() {

    }

    @Around("autoExchangeEnableMethods()")
    public Object handleExchange(ProceedingJoinPoint joinPoint) throws Throwable {
        Object originalResult = joinPoint.proceed();
        if (originalResult == null) {
            return null;
        }

        Object convertResult = applyExchangeStrategy.applyExchange(originalResult);
        if (convertResult != originalResult && convertResult != null) {
            return new TypedResultWrapper(originalResult, originalResult.getClass());
        }
        return originalResult;
    }
}
