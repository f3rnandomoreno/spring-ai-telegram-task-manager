package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.config.AllowedEmailsConfig;
import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import com.fmoreno.telegramtaskaiagent.service.WelcomeService;
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
public class TelegramClientConsumer implements LongPollingSingleThreadUpdateConsumer {

  final TelegramClient telegramClient;
  final NL2SQLAgent nl2SQLAgent;
  final TaskService taskService;
  final ManagerAgent managerAgent;
  final UserRepository userRepository;
  final WelcomeService welcomeService;

  public TelegramClientConsumer(
      TelegramClient telegramClient,
      NL2SQLAgent nl2SQLAgent,
      TaskService taskService,
      ManagerAgent managerAgent,
      UserRepository userRepository,
      WelcomeService welcomeService) {
    this.telegramClient = telegramClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
    this.managerAgent = managerAgent;
    this.userRepository = userRepository;
    this.welcomeService = welcomeService;
  }

  @Override
  public void consume(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      Long userId = update.getMessage().getFrom().getId();
      long chatId = update.getMessage().getChatId();
      String userName = getUserName(update);
      String messageText = update.getMessage().getText();

      Optional<UserEntity> userEntityOptional = userRepository.findByUserId(userId);

      if (userEntityOptional.isEmpty()) {
        handleNewUser(update, chatId);
        return;
      }

      handleExistingUser(update, chatId, userName, messageText);
    }
  }

  private String getUserName(Update update) {
    String userName = update.getMessage().getFrom().getFirstName();
    return userName.isEmpty() ? update.getMessage().getFrom().getUserName() : userName;
  }

  private void handleNewUser(Update update, long chatId) {
    String messageText = update.getMessage().getText();
    String email = extractEmail(messageText);

    if (email != null) {
      if (isAllowedEmail(email)) {
        createAndSaveNewUser(update, email);
        sendMessage(chatId, "You have been verified and added to the system.");
        welcomeService.showStartMessage(chatId);
      } else {
        sendMessage(chatId, "Your email is not in the list of allowed emails.");
      }
    } else {
      sendMessage(chatId, "Please provide your email for verification.");
    }
  }

  private String extractEmail(String messageText) {
    Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    Matcher matcher = emailPattern.matcher(messageText);
    return matcher.find() ? matcher.group() : null;
  }

  private boolean isAllowedEmail(String email) {
    return AllowedEmailsConfig.ALLOWED_EMAILS.contains(email.toLowerCase());
  }

  private void createAndSaveNewUser(Update update, String email) {
    UserEntity newUser = new UserEntity();
    newUser.setUserId(update.getMessage().getFrom().getId());
    newUser.setEmail(email);
    newUser.setFirstName(update.getMessage().getFrom().getFirstName());
    newUser.setLastName(update.getMessage().getFrom().getLastName());
    newUser.setUserName(update.getMessage().getFrom().getUserName());
    userRepository.save(newUser);
  }

  private void handleExistingUser(Update update, long chatId, String userName, String messageText) {
    log.info("Received message from {}: {}", update.getMessage().getFrom().getId(), messageText);

    if (isSpecialCommand(messageText, chatId)) {
      return;
    }

    String sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(messageText, userName);
    log.info("SQL Query: {}", sqlQuery);

    String executionResult = executeSQLQuery(sqlQuery);
    String chatResponse = managerAgent.processUserMessage(messageText, sqlQuery, executionResult, userName);

    sendMessage(chatId, chatResponse);
  }

  private boolean isSpecialCommand(String messageText, long chatId) {
    switch (messageText) {
      case "/ver_todas_las_tareas":
        welcomeService.handleVerTodasLasTareas(chatId);
        return true;
      case "/ver_mis_tareas":
        welcomeService.handleVerMisTareas(chatId);
        return true;
      case "/start":
        welcomeService.showStartMessage(chatId);
        return true;
      default:
        return false;
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
