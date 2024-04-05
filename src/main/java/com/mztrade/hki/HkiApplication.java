package com.mztrade.hki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SpringBootApplication
public class HkiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HkiApplication.class, args);
    }

}
