package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

@Log4j2
public class TelegramClientConfig implements LongPollingSingleThreadUpdateConsumer {

  final TelegramClient telegramClient;
  final NL2SQLAgent nl2SQLAgent;
  final TaskService taskService;
  final ManagerAgent managerAgent;
  final UserRepository userRepository;

  public TelegramClientConfig(
      TelegramClient telegramClient,
      NL2SQLAgent nl2SQLAgent,
      TaskService taskService,
      ManagerAgent managerAgent,
      UserRepository userRepository) {
    this.telegramClient = telegramClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
    this.managerAgent = managerAgent;
    this.userRepository = userRepository;
  }

  @Override
  public void consume(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Long userId = update.getMessage().getFrom().getId();
      Optional<UserEntity> userEntityOptional = userRepository.findByUserId(userId);

      if (userEntityOptional.isEmpty()) {
        // Ask for email and verify
        SendMessage message = SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text("Please provide your email for verification.")
            .build();
        try {
          telegramClient.execute(message);
        } catch (TelegramApiException e) {
          log.error("Error sending message to Telegram", e);
        }
        return;
      }

      UserEntity userEntity = userEntityOptional.get();
      log.info("Received message from {}: {}", userId, update.getMessage().getText());

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
      String userName = update.getMessage().getFrom().getFirstName();
      if (userName.isEmpty()) {
        userName = update.getMessage().getFrom().getUserName();
      }

      var chatResponse =
          managerAgent.processUserMessage(message_text, sqlQuery, executionResult, userName);
      SendMessage message = SendMessage.builder().chatId(chat_id).text(chatResponse).build();

      try {
        telegramClient.execute(message);
      } catch (TelegramApiException e) {
        log.error("Error sending message to Telegram", e);
      }
    }
  }
}
