package com.f3rnandomoreno.telegramtaskaiagent.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.f3rnandomoreno.telegramtaskaiagent.CommonTestIT;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class OpenAIClientTestIT extends CommonTestIT {

  @Autowired
  private ChatClient chatClient;

  // Test to check connection to OpenAI API
  @Test
  void testChatClient() {
    Prompt prompt = new Prompt("hello");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from OpenAI: {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).isNotEmpty();
    assertThat(content).contains("Hello");
  }
}
