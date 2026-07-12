package io.github.juwencheng.autoexchange.exchange;

import io.github.juwencheng.fieldtranslate.core.translate.TranslateContext;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateContextContributor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 汇率转换的上下文贡献者。从 HTTP 请求中解析目标币种并写入 TranslateContext。
 *
 * @author juwencheng
 */
public class ExchangeContextContributor implements TranslateContextContributor {

    private static final Logger log = LoggerFactory.getLogger(ExchangeContextContributor.class);

    private final ExchangeProperties properties;

    public ExchangeContextContributor(ExchangeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void contribute(HttpServletRequest request, TranslateContext context) {
        String targetCurrency = resolveTargetCurrency(request);
        context.setAttribute(ExchangeFieldTranslator.ATTR_TARGET_CURRENCY, targetCurrency);
    }

    private String resolveTargetCurrency(HttpServletRequest request) {
        try {
            String currencyFromParam = request.getParameter(properties.getTargetCurrencyParamName());
            if (currencyFromParam != null && !currencyFromParam.trim().isEmpty()) {
                return currencyFromParam;
            }
            String headerName = properties.getTargetCurrencyHeaderName();
            if (headerName != null && !headerName.isBlank()) {
                String currencyFromHeader = request.getHeader(headerName);
                if (currencyFromHeader != null && !currencyFromHeader.isBlank()) {
                    return currencyFromHeader;
                }
            }
        } catch (IllegalStateException e) {
            log.warn("当前线程没有绑定 RequestAttribute，使用默认的目标货币", e);
        }
        return properties.getDefaultTargetCurrency();
    }
}
