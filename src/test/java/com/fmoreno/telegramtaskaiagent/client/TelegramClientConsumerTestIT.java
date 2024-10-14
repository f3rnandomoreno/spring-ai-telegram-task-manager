package com.fmoreno.telegramtaskaiagent.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.persistence.UserRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import com.fmoreno.telegramtaskaiagent.service.WelcomeService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

class TelegramClientConsumerTestIT extends CommonTestIT {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private NL2SQLAgent nl2SQLAgent;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagerAgent managerAgent;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WelcomeService welcomeService;

    @MockBean
    private TelegramClient telegramClient;

    private TelegramClientConsumer telegramClientConsumer;

    @PostConstruct
    public void init() {
        telegramClientConsumer = new TelegramClientConsumer(telegramClient, nl2SQLAgent, taskService, managerAgent, userRepository, welcomeService);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
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
        //TODO use the RelevancyEvaluator to assert the response
        String expectedResponse = "aquí tienes la lista de tus tareas"; // Reemplaza con la respuesta esperada real
        assertThat(capturedMessage.getText().toLowerCase()).containsAnyOf(expectedResponse,"lista de tareas");
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

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

        // when
        telegramClientConsumer.consume(update);

        // then
        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getText()).contains("¡Bienvenido! Estas son tus opciones:");

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
        assertThat(capturedMessage.getText()).contains("Your email is not in the list of allowed emails.");

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
        assertThat(initialCapturedMessage.getText()).contains("Your email is not in the list of allowed emails.");

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
        assertThat(capturedMessage.getText()).isEqualTo("Tarea creada correctamente.");
    }

    @Test
    void testUpdateTaskResponse() throws Exception {
        // given
        String message = "Actualiza la tarea existente";
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
        assertThat(capturedMessage.getText()).isEqualTo("Tarea modificada correctamente.");
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
        assertThat(capturedMessage.getText()).isEqualTo("Tarea eliminada correctamente.");
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
        assertThat(capturedMessage.getText()).contains("las tareas");
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

    @Test
    void testHandleVerTodasLasTareas() throws Exception {
        // given
        String message = "/ver_todas_las_tareas";
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
        assertThat(capturedMessage.getText()).contains("Ejecutando comando: ver todas las tareas");
    }

    @Test
    void testHandleVerMisTareas() throws Exception {
        // given
        String message = "/ver_mis_tareas";
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
        assertThat(capturedMessage.getText()).contains("Ejecutando comando: ver mis tareas");
    }

    @Test
    void testWelcomeMessageOnChatOpen() throws Exception {
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
