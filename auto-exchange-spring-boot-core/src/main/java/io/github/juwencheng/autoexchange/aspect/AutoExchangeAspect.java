package io.github.juwencheng.autoexchange.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContext;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContextHolder;
import io.github.juwencheng.autoexchange.core.strategy.IApplyExchangeStrategy;

import javax.servlet.http.HttpServletRequest;

// 设置一个非常低的优先级（即一个非常大的数值）
// 这能确保我们的切面在大多数其他切面（如@Transactional, @Cacheable）之后执行
@Aspect
public class AutoExchangeAspect implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(AutoExchangeAspect.class);
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
        try {
            String targetCurrency = resolveTargetCurrency();
            AutoExchangeContextHolder.setContext(new AutoExchangeContext(targetCurrency));
            Object originalResult = joinPoint.proceed();
            if (originalResult == null) {
                return null;
            }
            applyExchangeStrategy.applyExchange(originalResult);
            return originalResult;
        } finally {
//            AutoExchangeContextHolder.clearContext();
        }
    }

    private String resolveTargetCurrency() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            // 如果请求地址中包含参数，则优先从参数中获取
            String currencyFromParam = request.getParameter(properties.getTargetCurrencyParamName());
            if (currencyFromParam != null && !currencyFromParam.trim().isEmpty()) {
                return currencyFromParam;
            }
            // --- 优先级查找逻辑保持不变 ---
            String headerName = properties.getTargetCurrencyHeaderName();
            if (headerName != null && !headerName.isBlank()) {
                String currencyFromHeader = request.getHeader(headerName);
                if (currencyFromHeader != null && !currencyFromHeader.isBlank()) {
                    return currencyFromHeader;
                }
            }
            return properties.getDefaultTargetCurrency();
        } catch (IllegalStateException e) {
            log.warn("当前线程没有绑定RequestAttribute，使用默认的目标货币", e);
        }
        return properties.getDefaultTargetCurrency();

    }

    @Override
    public int getOrder() {
        return this.properties.getAspectOrder();
    }
}
