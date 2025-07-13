package io.github.juwencheng.autoexchange.autoconfigure.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import io.github.juwencheng.autoexchange.autoconfigure.AutoExchangeAutoConfiguration;
import io.github.juwencheng.autoexchange.core.AutoExchangeProperties;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.autoexchange.service.ICurrencyExchangeService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@Import(AutoExchangeAutoConfiguration.class)
public class AutoExchangeAutoConfigurationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private AutoExchangeProperties autoExchangeProperties;

    @MockBean
    private IExchangeDataProvider exchangeDataProvider;

    @MockBean
    private ICurrencyExchangeService currencyExchangeService;
    @Test
    void shouldLoadConfigurationInWebApplication() {
        // 验证在Web应用环境下配置类能正确加载
        assertThat(applicationContext.getBeansOfType(AutoExchangeAutoConfiguration.class))
                .hasSize(1);
    }

    @Test
    void shouldCreateApplicationRunner() {
        // 验证ApplicationRunner Bean被创建
        assertThat(applicationContext.getBean(ApplicationRunner.class))
                .isNotNull()
                .isInstanceOf(ApplicationRunner.class);
    }
}
