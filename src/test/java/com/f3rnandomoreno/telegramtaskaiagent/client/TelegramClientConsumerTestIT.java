package com.f3rnandomoreno.telegramtaskaiagent.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.f3rnandomoreno.telegramtaskaiagent.CommonTestIT;
import com.f3rnandomoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.f3rnandomoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.f3rnandomoreno.telegramtaskaiagent.agents.NotificationAgent;
import com.f3rnandomoreno.telegramtaskaiagent.config.RelevancyEvaluatorConfigurations;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.TaskRepository;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.UserRepository;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.f3rnandomoreno.telegramtaskaiagent.service.MessageService;
import com.f3rnandomoreno.telegramtaskaiagent.service.TaskService;
import com.f3rnandomoreno.telegramtaskaiagent.service.WelcomeService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.Optional;

@Import(RelevancyEvaluatorConfigurations.class)
class TelegramClientConsumerTestIT extends CommonTestIT {

  @Value("${telegram.bot.token}")
  private String botToken;

  @Autowired private NL2SQLAgent nl2SQLAgent;

  @Autowired private TaskService taskService;

  @Autowired private TaskRepository taskRepository;

  @Autowired private ManagerAgent managerAgent;

  @Autowired private NotificationAgent notificationAgent;

  @Autowired private UserRepository userRepository;

  @Autowired private WelcomeService welcomeService;

  @MockBean private TelegramClient telegramClient;

  private TelegramClientConsumer telegramClientConsumer;

  @Autowired private MessageService messageService;

  @Autowired RelevancyEvaluator relevancyEvaluator;

  @PostConstruct
  public void init() throws TelegramApiException {
    telegramClientConsumer =
        new TelegramClientConsumer(
            telegramClient,
            nl2SQLAgent,
            taskService,
            managerAgent,
            userRepository,
            welcomeService,
            messageService,
            notificationAgent);
  }

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    taskRepository.deleteAll();
  }

  @Test
  void testGetTaskList() throws Exception {
    // given
    String message = "Aquí tienes la lista de tareas";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();

    // You can print the captured message to verify its content
    System.out.println("Mensaje capturado: " + capturedMessage.getText());

    // Ensure that the message text is as expected
    // TODO use the RelevancyEvaluator to assert the response
    String expectedResponse =
        "aquí tienes la lista de tus tareas"; // Reemplaza con la respuesta esperada real
    assertThat(capturedMessage.getText().toLowerCase())
        .containsAnyOf(expectedResponse, "lista de tareas");
  }

  @Test
  void testValidationWithAllowedEmail() throws Exception {
    // given
    String email = "allowed1@example.com";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(email);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage.getText()).contains("Hola TestUser");

    Optional<UserEntity> userEntityOptional = userRepository.findByEmail(email);
    assertThat(userEntityOptional).isPresent();
  }

  @Test
  void testValidationWithNotAllowedEmail() throws Exception {
    // given
    String email = "notallowed@example.com";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(email);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage.getText())
        .contains("Tu email no está en la lista de emails permitidos");

    Optional<UserEntity> userEntityOptional = userRepository.findByEmail(email);
    assertThat(userEntityOptional).isNotPresent();
  }

  @Test
  void testValidationWithNotAllowedEmailThenAllowedEmail() throws Exception {
    // given
    String initialMessage = "notallowed@example.com";
    Update initialUpdate = new Update();
    Message initialTelegramMessage = new Message();
    initialTelegramMessage.setText(initialMessage);
    initialTelegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    initialTelegramMessage.setFrom(user);
    initialUpdate.setMessage(initialTelegramMessage);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(initialUpdate);

    // then
    SendMessage initialCapturedMessage = argumentCaptor.getValue();
    assertThat(initialCapturedMessage).isNotNull();
    assertThat(initialCapturedMessage.getText())
        .contains("Tu email no está en la lista de emails permitidos");

    Optional<UserEntity> initialUserEntityOptional = userRepository.findByEmail(initialMessage);
    assertThat(initialUserEntityOptional).isNotPresent();

    // given
    String allowedEmail = "allowed1@example.com";
    Update allowedUpdate = new Update();
    Message allowedTelegramMessage = new Message();
    allowedTelegramMessage.setText(allowedEmail);
    allowedTelegramMessage.setChat(new Chat(9L, "private"));
    allowedTelegramMessage.setFrom(user);
    allowedUpdate.setMessage(allowedTelegramMessage);

    // when
    telegramClientConsumer.consume(allowedUpdate);

    // then
    SendMessage allowedCapturedMessage = argumentCaptor.getValue();
    assertThat(allowedCapturedMessage).isNotNull();
    assertThat(allowedCapturedMessage.getText()).contains("¡Bienvenido! Estas son tus opciones:");

    Optional<UserEntity> allowedUserEntityOptional = userRepository.findByEmail(allowedEmail);
    assertThat(allowedUserEntityOptional).isPresent();
  }

  @Test
  void testCreateTaskResponse() throws Exception {
    // given
    String message = "Crea una nueva tarea";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setChatId(1L);
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    // check the taskRepository to see if the task was created
    assertThat(taskRepository.count()).isEqualTo(1);
    var task = taskRepository.findAll().get(0);
    assertThat(task.getDescription()).isEqualTo("Nueva tarea");
    assertThat(task.getAssignee()).isBlank();
    // TODO check that notification is sent to all users
  }

  @Test
  void testUpdateTaskResponse() throws Exception {
    // given
    String message = "Actualiza la tarea 1 a tarea actualizada";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setChatId(1L);
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    // Create a task to update
    var task = new TaskEntity();
    task.setDescription("Tarea a actualizar");
    taskRepository.save(task);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    // check the taskRepository to see if the task was updated
    assertThat(taskRepository.count()).isEqualTo(1);
    var updatedTask = taskRepository.findAll().get(0);
    assertThat(updatedTask.getDescription()).containsIgnoringCase("Tarea actualizada");
    // TODO check that notification is sent to all users
  }

  @Test
  void testDeleteTaskResponse() throws Exception {
    // given
    String message = "Elimina la tarea existente";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setChatId(1L);
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    // check the task has been deleted
    assertThat(taskRepository.count()).isEqualTo(0);
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    // Evaluar la relevancia de la respuesta
    String responseContent = capturedMessage.getText();

    EvaluationRequest evaluationRequest =
        new EvaluationRequest(
            "Contiene algo sobre la eliminación de tareas existosa.",
            Collections.emptyList(),
            responseContent);
    EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

    // Verificar que la respuesta sea relevante
    assertTrue(
        evaluationResponse.isPass(),
        "score: "
            + evaluationResponse.getScore()
            + ", feedback: "
            + evaluationResponse.getFeedback());
  }

  @Test
  void testSelectQueryResponse() throws Exception {
    // given
    String message = "Muéstrame todas las tareas";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setChatId(1L);
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage.getText()).contains("Hola TestUser");
  }

  @Test
  void testShowStartMessage() throws Exception {
    // given
    String message = "/start";
    Update update = new Update();
    Message telegramMessage = new Message();
    telegramMessage.setText(message);
    telegramMessage.setChat(new Chat(9L, "private"));
    User user = new User(1L, "TestUser", false);
    telegramMessage.setFrom(user);
    update.setMessage(telegramMessage);

    // Add user to the test database
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(user.getId());
    userEntity.setEmail("allowed1@example.com");
    userEntity.setFirstName(user.getFirstName());
    userEntity.setLastName(user.getLastName());
    userEntity.setUserName(user.getUserName());
    userRepository.save(userEntity);

    ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
    org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

    // when
    telegramClientConsumer.consume(update);

    // then
    SendMessage capturedMessage = argumentCaptor.getValue();
    assertThat(capturedMessage).isNotNull();
    assertThat(capturedMessage.getText()).contains("¡Bienvenido! Estas son tus opciones:");
  }

}
