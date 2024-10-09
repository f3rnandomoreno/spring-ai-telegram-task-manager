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

    public String receiveMessageUser(String message) {
        String promptMessage="""
            Eres un asistente de gestión de tareas. Interpretas cuando un usuario quiere
            crear, ver, modificar o eliminar tareas. Identificas la tarea que hace falta cuando
            te dicen lo que necesitan en una frase corta. Por ejemplo "Crea la tarea jugar al basket
            y asígnala a Fernando".
            Mensaje usuario: %s
            """.formatted(message);
    return chatClient
        .prompt(new Prompt(promptMessage))
        .call()
        .chatResponse()
        .getResult()
        .getOutput()
        .getContent();
    }

    
}
