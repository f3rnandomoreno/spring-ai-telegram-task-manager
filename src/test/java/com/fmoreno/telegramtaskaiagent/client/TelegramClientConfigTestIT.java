package com.fmoreno.telegramtaskaiagent.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

class TelegramClientConfigTestIT extends CommonTestIT {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private NL2SQLAgent nl2SQLAgent;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagerAgent managerAgent;

    @MockBean
    private TelegramClient telegramClient;

    private TelegramClientConfig telegramClientConfig;

    @PostConstruct
    public void init() {
        telegramClientConfig = new TelegramClientConfig(telegramClient, nl2SQLAgent, taskService, managerAgent);
    }

    @Test
    void testGetTaskList() throws Exception {
        // given
        String message = "Aquí tienes la lista de tareas";
        Update update = new Update();
        Message telegramMessage = new Message();
        telegramMessage.setText(message);
        telegramMessage.setChat(new Chat(9L, "private"));
        update.setMessage(telegramMessage);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        org.mockito.Mockito.doReturn(null).when(telegramClient).execute(argumentCaptor.capture());

        // when
        telegramClientConfig.consume(update);

        // then
        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();

        // You can print the captured message to verify its content
        System.out.println("Mensaje capturado: " + capturedMessage.getText());

        // Ensure that the message text is as expected
        //TODO use the RelevancyEvaluator to assert the response
        String expectedResponse = "Aquí tienes la lista de tareas"; // Reemplaza con la respuesta esperada real
        assertThat(capturedMessage.getText()).contains(expectedResponse);
    }
}
