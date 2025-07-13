package io.github.juwencheng.autoexchange.autoconfigure.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.juwencheng.autoexchange.autoconfigure.annotation.EnableAutoExchange;

@SpringBootApplication
@EnableAutoExchange
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
