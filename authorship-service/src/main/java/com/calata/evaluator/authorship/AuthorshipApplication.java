package com.calata.evaluator.authorship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuthorshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorshipApplication.class, args);
    }
}
