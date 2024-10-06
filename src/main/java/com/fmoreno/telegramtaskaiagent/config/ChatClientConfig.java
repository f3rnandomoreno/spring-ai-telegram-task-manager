package com.fmoreno.telegramtaskaiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4o-mini")
                .withTemperature(0f)
                .withFunctions(Set.of("crearTarea", "modificarTarea", "eliminarTarea", "verTareas"))
                .build();
        return builder.defaultOptions(options).build();
    }
}
