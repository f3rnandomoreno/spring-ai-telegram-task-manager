package com.f3rnandomoreno.telegramtaskaiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RelevancyEvaluatorConfigurations {

    @Bean
    RelevancyEvaluator relevancyEvaluator(ChatClient.Builder chatClientBuilder) {
        return new RelevancyEvaluator(chatClientBuilder);
    }
}
