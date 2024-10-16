package com.fmoreno.telegramtaskaiagent.agents;

import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ManagerAgent {

  private final ChatClient chatClient;
  private final TaskService taskService;

  public ManagerAgent(ChatClient chatClient, TaskService taskService) {
    this.chatClient = chatClient;
    this.taskService = taskService;
  }

  public String processUserMessage(
      String messageText, String sqlQuery, String executionResult, String userName) {
    String responseMessage = generateResponseMessage(sqlQuery, executionResult);
    if (responseMessage != null) {
      responseMessage += taskService.executeSQLQuery("select * from tasks");
    } else {
      responseMessage = messageText;
    }
    String promptText = buildPrompt(responseMessage, sqlQuery, executionResult, userName);
    log.info("Prompt text: {}", promptText);
    return generateResponse(promptText);
  }

  private String buildPrompt(
      String messageText, String sqlQuery, String executionResult, String assignee) {
    return String.format(
        """
        \\¡Hola %s\\!
        He recibido tu mensaje:         ```
        %s        ```

        Aquí está la consulta SQL que he generado:         ```
        %s        ```

        Y este es el resultado de la ejecución:         ```
        %s        ```

        Instrucciones:
        1. Proporciona una respuesta amigable al usuario basada en esta información, utilizando su nombre `%s` para personalizar la respuesta y las consultas.
         2. Lista las tareas en un formato simplificado, siguiendo este patrón con una linea vacia entre tareas:
           *Tarea [ID]*: [Descripción] - _[Estado]_ - *[Asignada a]*
        3. El estado debe mostrarse en español: "Pendiente" para TODO, "En Progreso" para IN_PROGRESS, "Completada" para DONE.
        4. Incluye la fecha de última actualización solo si está disponible en el resultado de la ejecución, al final de la línea entre paréntesis.
        5. Cada tarea debe estar en una línea separada.
        6. No añadas numeración adicional ni viñetas.
        7. Ejemplo del formato deseado:
           *Tarea 8*: Leer documentos de la catequesis - _En Progreso_ - *Fernando*
        8. Si no hay fecha de actualización en el resultado, no incluyas esa parte.
        9. Asegúrate de que la respuesta sea clara y fácil de leer para el usuario.
        10. Basa tu respuesta ÚNICAMENTE en las tareas presentes en el "Resultado de la ejecución". No inventes ni añadas tareas que no estén en ese resultado.
        11. Enriquece el texto con emojis de forma coherente y atractiva para el usuario.
        """,
        assignee, messageText, sqlQuery, executionResult, assignee);
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

  private String generateResponseMessage(String sqlQuery, String executionResult) {
    String lowerCaseQuery = sqlQuery.toLowerCase().trim();
    if (lowerCaseQuery.startsWith("insert")) {
      return "Tarea creada correctamente.";
    } else if (lowerCaseQuery.startsWith("update")) {
      return "Tarea modificada correctamente.";
    } else if (lowerCaseQuery.startsWith("delete")) {
      return "Tarea eliminada correctamente.";
    }
    return null;
  }
}
