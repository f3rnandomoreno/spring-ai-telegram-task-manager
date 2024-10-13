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
        2. IMPORTANTE: Muestra las tareas utilizando ÚNICAMENTE el ID original de la base de datos.
           No añadas ninguna numeración adicional al listar las tareas.
        3. Formato para cada tarea:
           **Tarea ID [número]**
           - **Asignada a:** [nombre]
           - **Descripción:** [descripción]
           - **Estado:** [estado]
           - **Última actualización:** [fecha o "No disponible"]
        4. Asegúrate de que no haya números o viñetas adicionales antes del "Tarea ID".
        5. Mantén un espacio entre cada tarea para mejorar la legibilidad.
        6. Si es relevante, puedes mencionar el número total de tareas al principio o al final de la respuesta.
        7. Formatea la respuesta de manera clara y fácil de leer para el usuario.
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
