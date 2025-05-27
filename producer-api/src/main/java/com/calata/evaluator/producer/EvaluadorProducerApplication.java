package com.calata.evaluator.producer;

import com.calata.evaluator.producer.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(KafkaTopicProperties.class)
@SpringBootApplication
public class EvaluadorProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluadorProducerApplication.class, args);
    }
}
