package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.agents.ManagerAgent;
import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import jakarta.annotation.PostConstruct;

import static org.mockito.Mockito.doAnswer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

class TelegramClientConfigTestIT extends CommonTestIT {

   private final String botToken;
   private final ChatClient chatClient;
   private final NL2SQLAgent nl2SQLAgent;
   private final TaskService taskService;
   private final ManagerAgent managerAgent;
   private TelegramClientConfig telegramClientConfig;
   @SpyBean private TelegramClient telegramClient;

   @Autowired
   public TelegramClientConfigTestIT(
       @Value("${telegram.bot.token}") String botToken,
       ChatClient chatClient,
       NL2SQLAgent nl2SQLAgent,
       TaskService taskService,
       ManagerAgent managerAgent) {
       this.botToken = botToken;
       this.chatClient = chatClient;
       this.nl2SQLAgent = nl2SQLAgent;
       this.taskService = taskService;
       this.managerAgent = managerAgent;
   }

   @PostConstruct
   public void init() {
       telegramClientConfig = new TelegramClientConfig(botToken, chatClient, nl2SQLAgent, taskService, managerAgent);
   }

   // test message to see the task list
   @Test
   void testGetTaskList() throws TelegramApiException {
       // given
       String message = "dame la lista de tareas";
       Update update = new Update();
       Message telegramMessage = new Message();
       telegramMessage.setText(message);
       telegramMessage.setChat(new Chat(9L, "private"));
       update.setMessage(telegramMessage);

       // when telegramClient.execute() is called get the argument to check the message
       ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
       doAnswer(invocation -> {
           return "";
       }).when(telegramClient).execute(argumentCaptor.capture());

       // when
       telegramClientConfig.consume(update);

       // then
       SendMessage capturedMessage = argumentCaptor.getValue();
       Assertions.assertThat(capturedMessage.getText()).isEqualTo(message);
       Assertions.assertThat(capturedMessage).isNotNull();
   }
}
