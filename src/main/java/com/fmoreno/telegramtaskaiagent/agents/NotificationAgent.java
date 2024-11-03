package com.fmoreno.telegramtaskaiagent.agents;

import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.MessageService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.stringtemplate.v4.ST;

@Service
@Log4j2
public class NotificationAgent {

  private final UserRepository userRepository;
  private final MessageService messageService;
  private final ChatClient chatClient;
  private final String templateContent;

    public NotificationAgent(UserRepository userRepository, MessageService messageService, ChatClient chatClient) {
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.chatClient = chatClient;
        this.templateContent = loadTemplate();
    }

    private String loadTemplate() {
    try {
      ClassPathResource resource = new ClassPathResource("prompts/notification_prompt.st");
      Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
      return FileCopyUtils.copyToString(reader);
    } catch (IOException e) {
      log.error("Error loading template file", e);
      throw new RuntimeException("Could not load template file", e);
    }
  }

  public void processUserMessage(
      String messageText, String sqlQuery, String executionResult, String userName) {
    if (AgentHelper.isInsertOrUpdateOrDelete(sqlQuery)) {
      String responseMessage = generateResponse(buildPrompt(messageText, sqlQuery, executionResult, userName));
      if (responseMessage != null) {
        sendMessageToAllUsers(responseMessage);
      }
    }
  }

  private void sendMessageToAllUsers(String responseMessage) {
    List<UserEntity> users = userRepository.findAll();
    for (UserEntity user : users) {
      messageService.sendMessage(user.getChatId(), responseMessage);
    }
  }

  private String buildPrompt(
      String messageText, String sqlQuery, String executionResult, String assignee) {
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
}
