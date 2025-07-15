package io.github.juwencheng.autoexchange.core.interceptor;

import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContext;
import io.github.juwencheng.autoexchange.core.context.AutoExchangeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AutoExchange拦截器，用于处理AutoExchangeContext的创建和清空
 */
public class AutoExchangeInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AutoExchangeInterceptor.class);
    private final AutoExchangeProperties properties;

    public AutoExchangeInterceptor(AutoExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String targetCurrency = resolveTargetCurrency(request);
        AutoExchangeContextHolder.setContext(new AutoExchangeContext(targetCurrency));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，防止内存泄漏
        AutoExchangeContextHolder.clearContext();
    }

    private String resolveTargetCurrency(HttpServletRequest request) {
        try {
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
}
