package com.fmoreno.telegramtaskaiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean("sqlChatClient")
    public ChatClient chatClient(ChatClient.Builder builder) {
    OpenAiChatOptions options =
        OpenAiChatOptions.builder()
            .withModel("gpt-4o-mini")
            .withTemperature(0.0)
            .build();
        return builder.defaultOptions(options).build();
    }

    @Bean("managerChatClient")
    public ChatClient chatClientManager(ChatClient.Builder builder) {
        OpenAiChatOptions options =
                OpenAiChatOptions.builder()
                        .withModel("gpt-4o-mini")
                        .withTemperature(0.0)
                        .build();
        return builder.defaultOptions(options).build();
    }
}
