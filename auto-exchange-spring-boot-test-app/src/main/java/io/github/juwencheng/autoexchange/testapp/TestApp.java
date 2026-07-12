package io.github.juwencheng.autoexchange.testapp;

import io.github.juwencheng.fieldtranslate.autoconfigure.annotation.EnableFieldTranslate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFieldTranslate
@EnableScheduling
public class TestApp {
    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
