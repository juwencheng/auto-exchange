package io.github.juwencheng.autoexchange.core.translate;

import org.springframework.core.NamedThreadLocal;

/**
 * 通用翻译上下文的 ThreadLocal 持有者。
 * <p>
 * 生命周期由拦截器管理：preHandle 中创建，afterCompletion 中清理。
 *
 * @author juwencheng
 */
public class TranslateContextHolder {

    private static final ThreadLocal<TranslateContext> contextHolder =
            new NamedThreadLocal<>("Translate Context Holder");

    private TranslateContextHolder() {
    }

    public static void setContext(TranslateContext context) {
        contextHolder.set(context);
    }

    public static TranslateContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
