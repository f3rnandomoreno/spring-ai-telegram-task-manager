package com.fmoreno.telegramtaskaiagent.openai;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Log4j2
public class ITClientTest {

    @Autowired
    ChatClient chatClient;

    // create a test that use the openai client to test the connection to the openai api
    @Test
    void testChatClient() {
        Prompt prompt = new Prompt("hola");
        log.info("Prompt: {}", prompt);
        String content = chatClient.prompt(prompt).call().content();
        log.info("Content from openai: {}", content);
        assertThat(content).isNotBlank();
        assertThat(content).isNotEmpty();
        assertThat(content).contains("Hola");
    }
}
