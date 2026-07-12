package io.github.juwencheng.autoexchange.autoconfigure.test;

import io.github.juwencheng.autoexchange.autoconfigure.ExchangeAutoConfiguration;
import io.github.juwencheng.autoexchange.exchange.ExchangeFieldTranslator;
import io.github.juwencheng.autoexchange.provider.IExchangeDataProvider;
import io.github.juwencheng.fieldtranslate.autoconfigure.FieldTranslateAutoConfiguration;
import io.github.juwencheng.fieldtranslate.core.translate.TranslateStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@Import({FieldTranslateAutoConfiguration.class, ExchangeAutoConfiguration.class})
public class ExchangeAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private IExchangeDataProvider exchangeDataProvider;

    @Test
    void shouldLoadExchangeBeansInWebApplication() {
        assertThat(applicationContext.getBeansOfType(ExchangeAutoConfiguration.class)).hasSize(1);
        assertThat(applicationContext.getBean(ExchangeFieldTranslator.class)).isNotNull();
        assertThat(applicationContext.getBean(TranslateStrategy.class)).isNotNull();
    }
}
