package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.config.AllowedEmailsConfig;
import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import com.fmoreno.telegramtaskaiagent.service.WelcomeService;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
@Component
public class TelegramClientConsumer implements LongPollingSingleThreadUpdateConsumer {

  @Value("${telegram.bot.token}")
  protected String botToken;

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

  @PostConstruct
  public void init() throws TelegramApiException {
    TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
    botsApplication.registerBot(
        botToken,
        new TelegramClientConsumer(
            telegramClient,
            nl2SQLAgent,
            taskService,
            managerAgent,
            userRepository,
            welcomeService));
    log.info("Telegram bot initialized");
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

      handleExistingUser(update.getMessage().getFrom().getId(), chatId, userName, messageText);
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
        sendMessage(chatId, "Has sido verificado y añadido al sistema.");
        welcomeService.showStartMessage(chatId);
      } else {
        sendMessage(chatId, "Tu email no está en la lista de emails permitidos.");
      }
    } else {
      sendMessage(chatId, "Por favor, introduce tu email para verificar tu usuario.");
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
    newUser.setChatId(update.getMessage().getChatId());
    newUser.setEmail(email);
    newUser.setFirstName(update.getMessage().getFrom().getFirstName());
    newUser.setLastName(update.getMessage().getFrom().getLastName());
    newUser.setUserName(update.getMessage().getFrom().getUserName());
    userRepository.save(newUser);
  }

  private void handleExistingUser(Long userId, long chatId, String userName, String messageText) {
    log.info("Received message from {}: {}", userId, messageText);

    var _message = isSpecialCommand(messageText);
    if (_message == null) {
      _message = messageText;
    }
    if(messageText.equalsIgnoreCase("ayuda")){
        welcomeService.showStartMessage(chatId);
        return;
    }

    String sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(_message, userName);
    log.info("SQL Query: {}", sqlQuery);

    String executionResult = executeSQLQuery(sqlQuery);
    String chatResponse =
        managerAgent.processUserMessage(_message, sqlQuery, executionResult, userName);

    sendMessage(chatId, chatResponse);
  }

  private String isSpecialCommand(String messageText) {
    switch (messageText) {
      case "/ver_todas_las_tareas":
        return "ver todas la tareas";
      case "/ver_mis_tareas":
        return "ver mis tareas";
      case "/start":
        return "start";
      default:
        return null;
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
    SendMessage message = SendMessage.builder().chatId(chatId).text(text).build();
    // replace special characters in MarkdownV2
    message.setText(message.getText().replace("!", "\\!"));
    message.setText(message.getText().replace(".", "\\."));
    message.setText(message.getText().replace("-", "\\-"));
    message.setText(message.getText().replace("(", "\\("));
    message.setText(message.getText().replace(")", "\\)"));

    message.setParseMode("MarkdownV2");
    try {
      telegramClient.execute(message);
    } catch (TelegramApiException e) {
      log.error("Error sending message to Telegram", e);
    }
  }
}
