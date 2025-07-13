package tech.baizi.autoexchange.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.baizi.autoexchange.autoconfigure.annotation.EnableAutoExchange;

@SpringBootApplication
@EnableAutoExchange
@EnableScheduling
public class TestApp {
    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
