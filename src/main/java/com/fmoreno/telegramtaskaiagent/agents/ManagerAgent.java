package com.fmoreno.telegramtaskaiagent.agents;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ManagerAgent {

  private final ChatClient chatClient;

  public ManagerAgent(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  public String processUserMessage(String messageText, String sqlQuery, String executionResult) {
    String promptText = buildPrompt(messageText, sqlQuery, executionResult);
    log.info("Prompt text: {}", promptText);
    return generateResponse(promptText);
  }

  private String buildPrompt(String messageText, String sqlQuery, String executionResult) {
    return String.format("""
        Mensaje del usuario: %s

        Consulta SQL generada: %s

        Resultado de la ejecución: %s

        Instrucciones:
        1. Proporciona una respuesta amigable al usuario basada en esta información.
        2. Lista las tareas en un formato simplificado, siguiendo este patrón:
           Tarea [ID]: [Descripción] - [Estado] - ([Asignada a])
        3. El estado debe mostrarse en español: "Pendiente" para TODO, "En Progreso" para IN_PROGRESS, "Completada" para DONE.
        4. Incluye la fecha de última actualización solo si está disponible, al final de la línea entre paréntesis.
        5. Cada tarea debe estar en una línea separada.
        6. No añadas numeración adicional ni viñetas.
        7. Si es relevante, menciona el número total de tareas al principio o al final de la respuesta.
        8. Ejemplo del formato deseado:
           Tarea 8: Leer documentos de la catequesis - En Progreso - (Fernando) (Actualizado: 2024-10-13)
        9. Si no hay fecha de actualización, no incluyas esa parte.
        10. Asegúrate de que la respuesta sea clara y fácil de leer para el usuario.
        """,
        messageText, sqlQuery, executionResult);
  }

  private String generateResponse(String promptText) {
    return chatClient
        .prompt(new Prompt(promptText))
        .call()
        .chatResponse()
        .getResult()
        .getOutput()
        .getContent();
  }
}
