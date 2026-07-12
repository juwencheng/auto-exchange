package io.github.juwencheng.autoexchange.openapi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 汇率转换 OpenAPI 模块自动配置。仅在 springdoc-openapi 存在时激活。
 *
 * @author juwencheng
 */
@AutoConfiguration
@ConditionalOnClass(OperationCustomizer.class)
public class ExchangeOpenApiAutoConfiguration {

    @Bean
    public ExchangeOperationCustomizer exchangeOperationCustomizer() {
        return new ExchangeOperationCustomizer();
    }
}
