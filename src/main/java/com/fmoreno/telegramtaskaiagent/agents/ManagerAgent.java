package com.fmoreno.telegramtaskaiagent.agents;

import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Log4j2
@Service
public class ManagerAgent {

    private final ChatClient chatClient;
    private final TaskService taskService;
    private final String templateContent;
    private final NotificationAgent notificationAgent;

    public ManagerAgent(ChatClient chatClient, TaskService taskService, NotificationAgent notificationAgent) {
        this.chatClient = chatClient;
        this.taskService = taskService;
        this.notificationAgent = notificationAgent;
        this.templateContent = loadTemplate();
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/manager_prompt.st");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error("Error loading template file", e);
            throw new RuntimeException("Could not load template file", e);
        }
    }

    public String processUserMessage(String messageText, String sqlQuery, String executionResult, String userName) {
        String responseMessage = generateResponseMessage(sqlQuery, executionResult);
        if (responseMessage != null) {
            responseMessage += taskService.executeSQLQuery("select * from tasks");
        } else {
            responseMessage = messageText;
        }
        String promptText = buildPrompt(responseMessage, sqlQuery, executionResult, userName);
        log.info("Prompt text: {}", promptText);
        String response = generateResponse(promptText);

        // Call NotificationAgent to generate and send notifications
        notificationAgent.sendNotification(sqlQuery, executionResult);

        return response;
    }

    private String buildPrompt(String messageText, String sqlQuery, String executionResult, String assignee) {
        ST template = new ST(templateContent);
        template.add("messageText", messageText);
        template.add("sqlQuery", sqlQuery);
        template.add("executionResult", executionResult);
        template.add("assignee", assignee);
        return template.render();
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
