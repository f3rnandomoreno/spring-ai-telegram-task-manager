package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
public class TelegramClientConfig implements LongPollingSingleThreadUpdateConsumer {

  final TelegramClient telegramClient;
  final ChatClient chatClient;
  final NL2SQLAgent nl2SQLAgent;
  final TaskService taskService;
  final ManagerAgent managerAgent;

  public TelegramClientConfig(
      TelegramClient telegramClient,
      ChatClient chatClient,
      NL2SQLAgent nl2SQLAgent,
      TaskService taskService,
      ManagerAgent managerAgent) {
    this.telegramClient = telegramClient;
    this.chatClient = chatClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
    this.managerAgent = managerAgent;
  }

  @Override
  public void consume(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      log.info("Received message: {}", update.getMessage().getText());

      String sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(update.getMessage().getText());
      log.info("SQL Query: {}", sqlQuery);

      String executionResult = "";
      if (!sqlQuery.isEmpty()) {
        try {
          executionResult = taskService.executeSQLQuery(sqlQuery);
          log.info("SQL Query executed successfully: {}", sqlQuery);
        } catch (Exception e) {
          log.error("Error executing SQL query: {}", e.getMessage());
          executionResult = "Error al ejecutar la consulta: " + e.getMessage();
        }
      }

      String message_text = update.getMessage().getText();
      long chat_id = update.getMessage().getChatId();
      var chatResponse = managerAgent.receiveMessageUser(message_text, sqlQuery, executionResult);
      SendMessage message = SendMessage.builder().chatId(chat_id).text(chatResponse).build();

      try {
        telegramClient.execute(message);
      } catch (TelegramApiException e) {
        log.error("Error sending message to Telegram", e);
      }
    }
  }
}
