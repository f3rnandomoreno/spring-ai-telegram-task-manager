package com.fmoreno.telegramtaskaiagent.agents;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ManagerAgent {

    final private ChatClient chatClient;

    public String receiveMessageUser(String message_text, String sqlQuery, String executionResult) {
        String promptText =
                String.format(
                        "Mensaje del usuario: %s\n\nConsulta SQL generada: %s\n\nResultado de la ejecución: %s\n\nPor favor, proporciona una respuesta amigable al usuario basada en esta información.",
                        message_text, sqlQuery, executionResult);
    return chatClient
        .prompt(new Prompt(promptText))
        .call()
        .chatResponse()
        .getResult()
        .getOutput()
        .getContent();
    }

    
}
