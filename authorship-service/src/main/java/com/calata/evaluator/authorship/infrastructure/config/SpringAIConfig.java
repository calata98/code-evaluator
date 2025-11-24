package com.calata.evaluator.authorship.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {
    @Bean
    ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient.create(model);
    }
}
