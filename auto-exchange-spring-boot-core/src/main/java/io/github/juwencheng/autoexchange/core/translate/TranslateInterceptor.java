package io.github.juwencheng.autoexchange.core.translate;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 通用翻译拦截器。管理 {@link TranslateContext} 的生命周期，
 * 并通过 {@link TranslateContextContributor} 让各插件贡献上下文属性。
 *
 * @author juwencheng
 */
public class TranslateInterceptor implements HandlerInterceptor {

    private final List<TranslateContextContributor> contributors;

    public TranslateInterceptor(List<TranslateContextContributor> contributors) {
        this.contributors = contributors != null ? contributors : List.of();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TranslateContext context = new TranslateContext();
        for (TranslateContextContributor contributor : contributors) {
            contributor.contribute(request, context);
        }
        TranslateContextHolder.setContext(context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TranslateContextHolder.clearContext();
    }
}
