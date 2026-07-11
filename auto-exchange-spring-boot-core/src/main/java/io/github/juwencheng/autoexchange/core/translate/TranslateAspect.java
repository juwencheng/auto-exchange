package io.github.juwencheng.autoexchange.core.translate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

/**
 * 通用翻译 AOP 切面。拦截标注了 {@link TranslateResponse} 的方法，
 * 在方法返回后执行通用翻译策略。
 *
 * @author juwencheng
 */
@Aspect
public class TranslateAspect implements Ordered {

    private final TranslateStrategy translateStrategy;
    private final int order;

    public TranslateAspect(TranslateStrategy translateStrategy, int order) {
        this.translateStrategy = translateStrategy;
        this.order = order;
    }

    @Pointcut("@annotation(io.github.juwencheng.autoexchange.core.translate.TranslateResponse)")
    public void translateResponse() {
    }

    @Around("translateResponse()")
    public Object handleTranslation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result == null) {
            return null;
        }
        translateStrategy.applyTranslation(result);
        return result;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
