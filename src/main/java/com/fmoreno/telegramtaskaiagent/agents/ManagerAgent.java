package com.fmoreno.telegramtaskaiagent.agents;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ManagerAgent {

  private final ChatClient chatClient;

  public ManagerAgent(@Qualifier("managerChatClient")ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  public String receiveMessageUser(String message_text, String sqlQuery, String executionResult) {
    String promptText =
        String.format(
            "Mensaje del usuario: %s\n\nConsulta SQL generada: %s\n\nResultado de la ejecución: %s\n\nPor favor, proporciona una respuesta amigable al usuario basada en esta información.",
            message_text, sqlQuery, executionResult);
    log.info("Prompt text: {}", promptText);
    return chatClient
        .prompt(new Prompt(promptText))
        .call()
        .chatResponse()
        .getResult()
        .getOutput()
        .getContent();
  }
}
