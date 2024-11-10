package com.f3rnandomoreno.telegramtaskaiagent.client;

import com.f3rnandomoreno.telegramtaskaiagent.agents.NotesAgent;
import com.f3rnandomoreno.telegramtaskaiagent.agents.NotificationAgent;
import com.f3rnandomoreno.telegramtaskaiagent.service.MessageService;
import com.f3rnandomoreno.telegramtaskaiagent.service.NotesService;
import com.f3rnandomoreno.telegramtaskaiagent.service.WelcomeService;
import com.f3rnandomoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.f3rnandomoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.f3rnandomoreno.telegramtaskaiagent.config.AllowedEmailsConfig;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.UserRepository;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.f3rnandomoreno.telegramtaskaiagent.service.TaskService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
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
  final MessageService messageService;
  final NotificationAgent notificationAgent;
  final NotesService notesService;
  final NotesAgent notesAgent;

  public TelegramClientConsumer(
          TelegramClient telegramClient,
          NL2SQLAgent nl2SQLAgent,
          TaskService taskService,
          ManagerAgent managerAgent,
          UserRepository userRepository,
          WelcomeService welcomeService,
          MessageService messageService,
          NotificationAgent notificationAgent,
          NotesService notesService,
          NotesAgent chatService) {
    this.telegramClient = telegramClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
    this.managerAgent = managerAgent;
    this.userRepository = userRepository;
    this.welcomeService = welcomeService;
    this.messageService = messageService;
    this.notificationAgent = notificationAgent;
    this.notesService = notesService;
    this.notesAgent = chatService;
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
            welcomeService,
            messageService,
            notificationAgent,
            notesService,
            notesAgent));
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
        messageService.sendMessage(chatId, "Has sido verificado y añadido al sistema.");
        welcomeService.showStartMessage(chatId);
      } else {
        messageService.sendMessage(chatId, "Tu email no está en la lista de emails permitidos.");
      }
    } else {
      messageService.sendMessage(chatId, "Por favor, introduce tu email para verificar tu usuario.");
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

    if (messageText.startsWith("/add_note ")) {
      handleAddNote(chatId, messageText);
      return;
    } else if (messageText.equals("/show_notes")) {
      handleShowNotes(chatId);
      return;
    } else if (messageText.startsWith("/show_note ")) {
      handleShowNote(chatId, messageText);
      return;
    } else if (messageText.startsWith("/chat_note ")) {
      handleChatNote(chatId, messageText);
      return;
    }

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

    messageService.sendMessage(chatId, chatResponse);

    notificationAgent.processUserMessage(sqlQuery, executionResult);

  }

  private void handleAddNote(long chatId, String messageText) {
    String[] parts = messageText.split(" ", 3);
    if (parts.length < 3) {
      messageService.sendMessage(chatId, "Por favor, proporciona un nombre y contenido para la nota\\.");
      return;
    }

    String noteName = parts[1];
    String content = parts[2];
    
    notesService.saveNote(noteName, content);
    messageService.sendMessage(chatId, String.format("✅ Nota '%s' guardada correctamente", noteName));
  }

  private void handleShowNotes(long chatId) {
    List<String> notes = notesService.listNotes();
    if (notes.isEmpty()) {
      messageService.sendMessage(chatId, "No hay notas guardadas\\.");
      return;
    }

    StringBuilder message = new StringBuilder("📝 *Notas disponibles*:\n");
    notes.forEach(note -> message.append("• ").append(escapeMarkdown(note)).append("\n"));
    messageService.sendMessage(chatId, message.toString());
  }

  private void handleShowNote(long chatId, String messageText) {
    String[] parts = messageText.split(" ", 2);
    if (parts.length < 2) {
      messageService.sendMessage(chatId, "Por favor, especifica el nombre de la nota\\.");
      return;
    }

    String noteName = parts[1];
    String content = notesService.getNoteContent(noteName);
    
    if (content == null) {
      messageService.sendMessage(chatId, "No se encontró la nota especificada\\.");
      return;
    }
    messageService.sendMessage(chatId, content);
  }

  private void handleChatNote(long chatId, String messageText) {
    String[] parts = messageText.split(" ", 3);
    if (parts.length < 3) {
      messageService.sendMessage(chatId, 
          "Por favor, especifica el nombre de la nota y tu pregunta\\.");
      return;
    }

    String noteName = parts[1];
    String question = parts[2];
    
    String response = notesAgent.chatAboutNote(noteName, question);
    messageService.sendMessage(chatId, response);
  }

  private String isSpecialCommand(String messageText) {
    switch (messageText) {
      case "/ver_todas_las_tareas":
        return "ver todas la tareas";
      case "/ver_mis_tareas":
        return "ver mis tareas";
      case "/start":
        return "start";
      case "/show_notes":
      case "/add_note":
      case "/show_note":
      case "/chat_note":
        return messageText;
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

  private String escapeMarkdown(String text) {
    return text.replace("_", "\\_")
              .replace("*", "\\*")
              .replace("[", "\\[")
              .replace("]", "\\]")
              .replace("(", "\\(")
              .replace(")", "\\)")
              .replace("~", "\\~")
              .replace("`", "\\`")
              .replace(">", "\\>")
              .replace("#", "\\#")
              .replace("+", "\\+")
              .replace("-", "\\-")
              .replace("=", "\\=")
              .replace("|", "\\|")
              .replace("{", "\\{")
              .replace("}", "\\}")
              .replace(".", "\\.")
              .replace("!", "\\!");
  }
}
