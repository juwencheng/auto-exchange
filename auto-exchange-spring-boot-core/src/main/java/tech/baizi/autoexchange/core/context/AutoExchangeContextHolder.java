package tech.baizi.autoexchange.core.context;

import org.springframework.core.NamedThreadLocal;

/**
 * AutoExchange上下文的Holder
 */
public class AutoExchangeContextHolder {
    private static final ThreadLocal<AutoExchangeContext> contextHolder = new NamedThreadLocal<>("AutoExchange Context Holder");

    public AutoExchangeContextHolder() {
    }

    public static void setContext(AutoExchangeContext context) {
        contextHolder.set(context);
    }

    public static AutoExchangeContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

}
