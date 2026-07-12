package io.github.juwencheng.autoexchange.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.juwencheng.autoexchange.autoconfigure.annotation.EnableAutoExchange;
import io.github.juwencheng.autoexchange.openapi.TranslateOpenApiAutoConfiguration;

@SpringBootApplication
@EnableAutoExchange
@EnableScheduling
@Import(TranslateOpenApiAutoConfiguration.class)
public class TestApp {
    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
