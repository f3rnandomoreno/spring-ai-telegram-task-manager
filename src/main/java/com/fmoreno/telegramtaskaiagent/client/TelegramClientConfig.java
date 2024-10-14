package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.config.AllowedEmailsConfig;
import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      long chat_id = update.getMessage().getChatId();
      String userName = update.getMessage().getFrom().getFirstName();
      Optional<UserEntity> userEntityOptional = userRepository.findByUserId(userId);

      if (userEntityOptional.isEmpty()) {
        String messageText = update.getMessage().getText();
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(messageText);
        if (matcher.find()) {
          String email = matcher.group();
          if (AllowedEmailsConfig.ALLOWED_EMAILS.contains(email.toLowerCase())) {
            UserEntity newUser = new UserEntity();
            newUser.setUserId(userId);
            newUser.setEmail(email);
            newUser.setFirstName(update.getMessage().getFrom().getFirstName());
            newUser.setLastName(update.getMessage().getFrom().getLastName());
            newUser.setUserName(update.getMessage().getFrom().getUserName());
            userRepository.save(newUser);
            sendMessage(update.getMessage().getChatId(), "You have been verified and added to the system.");
          } else {
            sendMessage(update.getMessage().getChatId(), "Your email is not in the list of allowed emails.");
          }
        } else {
          sendMessage(update.getMessage().getChatId(), "Please provide your email for verification.");
        }
        return;
      }

      if (userName.isEmpty()) {
        userName = update.getMessage().getFrom().getUserName();
      }
      log.info("Received message from {}: {}", userId, update.getMessage().getText());

      String sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(update.getMessage().getText(), userName);
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

  private void sendMessage(Long chatId, String text) {
    SendMessage message = SendMessage.builder()
        .chatId(chatId)
        .text(text)
        .build();
    try {
      telegramClient.execute(message);
    } catch (TelegramApiException e) {
      log.error("Error sending message to Telegram", e);
    }
  }
}
