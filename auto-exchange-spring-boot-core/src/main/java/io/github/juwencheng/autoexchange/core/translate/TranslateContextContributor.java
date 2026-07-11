package io.github.juwencheng.autoexchange.core.translate;

import javax.servlet.http.HttpServletRequest;

/**
 * 翻译上下文贡献者接口。各翻译插件通过实现此接口来向 {@link TranslateContext}
 * 添加请求级别的上下文属性（如目标币种、locale 等）。
 *
 * @author juwencheng
 */
public interface TranslateContextContributor {

    /**
     * 在请求开始时向翻译上下文中贡献属性
     *
     * @param request HTTP 请求
     * @param context 翻译上下文
     */
    void contribute(HttpServletRequest request, TranslateContext context);
}
