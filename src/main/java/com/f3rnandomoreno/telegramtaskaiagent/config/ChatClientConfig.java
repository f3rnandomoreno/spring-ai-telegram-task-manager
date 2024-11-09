package com.f3rnandomoreno.telegramtaskaiagent.config;

import org.springframework.ai.autoconfigure.ollama.OllamaChatProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatClientConfig {

    @Value("${chat-client.read-timeout}") private int chatClientTimeout;
    @Value("${chat-client.connect-timeout}") private int chatClientConnectTimeout;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        OllamaChatProperties properties = new OllamaChatProperties();
        return builder.build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(Duration.ofSeconds(chatClientConnectTimeout))
                        .withReadTimeout(Duration.ofSeconds(chatClientTimeout))));
    }
}
