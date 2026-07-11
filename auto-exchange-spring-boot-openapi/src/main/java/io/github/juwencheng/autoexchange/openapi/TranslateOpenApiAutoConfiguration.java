package io.github.juwencheng.autoexchange.openapi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 模块自动配置。仅在 springdoc-openapi 存在时激活。
 * <p>
 * 通过 {@link TranslateOperationCustomizer} 自动为标注了翻译注解的
 * Controller 方法增强其 OpenAPI 文档描述。
 *
 * @author juwencheng
 */
@Configuration
@ConditionalOnClass(OperationCustomizer.class)
public class TranslateOpenApiAutoConfiguration {

    @Bean
    public TranslateOperationCustomizer translateOperationCustomizer() {
        return new TranslateOperationCustomizer();
    }
}
