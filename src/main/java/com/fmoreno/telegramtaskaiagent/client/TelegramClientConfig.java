package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
public class TelegramClientConfig implements LongPollingSingleThreadUpdateConsumer {

  private final TelegramClient telegramClient;
  private final ChatClient chatClient;
  private final NL2SQLAgent nl2SQLAgent;
  private final TaskService taskService;

  public TelegramClientConfig(
      String botToken, ChatClient chatClient, NL2SQLAgent nl2SQLAgent, TaskService taskService) {
    this.telegramClient = new OkHttpTelegramClient(botToken);
    this.chatClient = chatClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
  }

  @Override
  public void consume(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      processMessage(update);
    }
  }

  private void processMessage(Update update) {
    try {
      log.info("Received message: {}", update.getMessage().getText());

      String sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(update.getMessage().getText());
      log.info("SQL Query: {}", sqlQuery);

      String executionResult = executeSQLQuery(sqlQuery);

      log.info("executionResult: {}", executionResult);

      String promptText = createPromptText(update.getMessage().getText(), sqlQuery, executionResult);

      sendMessageToTelegram(promptText, update.getMessage().getChatId());

    } catch (Exception e) {
      handleProcessingError(e, update.getMessage().getChatId());
    }
  }

  private String executeSQLQuery(String sqlQuery) {
    if (sqlQuery.isEmpty()) {
      return "";
    }
    try {
      String result = taskService.executeSQLQuery(sqlQuery);
      log.info("SQL Query executed successfully: {}", sqlQuery);
      return result;
    } catch (Exception e) {
      log.error("Error executing SQL query: {}", e.getMessage());
      return "Error al ejecutar la consulta: " + e.getMessage();
    }
  }

  private String createPromptText(String messageText, String sqlQuery, String executionResult) {
    return String.format(
        "Mensaje del usuario: %s\n\nConsulta SQL generada: %s\n\nResultado de la ejecución: %s\n\nPor favor, proporciona una respuesta amigable al usuario basada en esta información.",
        messageText, sqlQuery, executionResult);
  }

  private void sendMessageToTelegram(String promptText, long chatId) {
    Prompt prompt = new Prompt(promptText);
    SendMessage message = SendMessage.builder()
        .chatId(chatId)
        .text(chatClient.prompt(prompt).call().content())
        .build();
    try {
      telegramClient.execute(message);
    } catch (TelegramApiException e) {
      log.error("Error sending message to Telegram", e);
    }
  }

  private void handleProcessingError(Exception e, long chatId) {
    SendMessage message = SendMessage.builder()
        .chatId(chatId)
        .text("Error processing message")
        .build();
    try {
      telegramClient.execute(message);
    } catch (TelegramApiException telegramApiException) {
      log.error("Error sending message to Telegram", telegramApiException);
    }
    log.error("Error processing message", e);
  }
}