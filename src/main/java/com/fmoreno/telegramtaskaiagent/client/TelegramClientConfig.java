package com.fmoreno.telegramtaskaiagent.client;

import com.fmoreno.telegramtaskaiagent.agents.NL2SQLAgent;
import com.fmoreno.telegramtaskaiagent.service.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
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

  public TelegramClientConfig(
      String botToken, ChatClient chatClient, NL2SQLAgent nl2SQLAgent, TaskService taskService) {
    telegramClient = new OkHttpTelegramClient(botToken);
    this.chatClient = chatClient;
    this.nl2SQLAgent = nl2SQLAgent;
    this.taskService = taskService;
  }

  @Override
  public void consume(Update update) {
    // We check if the update has a message and the message has text
    if (update.hasMessage() && update.getMessage().hasText()) {
      log.info("Received message: {}", update.getMessage().getText());
      // Set variables
      // then we need to get the sql query from the text of the message
      var sqlQuery = "";
      sqlQuery = nl2SQLAgent.processNaturalLanguageToSQL(update.getMessage().getText());
      log.info("SQL Query: {}", sqlQuery);
      // if there is sql query in the message, we need to execute
      if (!sqlQuery.isEmpty()) {
        try {
          // Assuming there's a service or repository that handles database operations
          // This is a placeholder for the actual database execution logic
          // For example, if you have a TaskService with a method executeSQLQuery
          taskService.executeSQLQuery(sqlQuery);
          log.info("SQL Query executed successfully: {}", sqlQuery);
        } catch (Exception e) {
          log.error("Error executing SQL query: {}", e.getMessage());
        }
      }
      // when it is executed mix the message received with the sql query result and
      // send to an agent that will process the message and response to the user with the
      // change done.

      String message_text = update.getMessage().getText();
      long chat_id = update.getMessage().getChatId();

      Prompt prompt = new Prompt(message_text);

      SendMessage message =
          SendMessage // Create a message object
              .builder()
              .chatId(chat_id)
              .text(chatClient.prompt(prompt).call().content())
              .build();
      try {
        telegramClient.execute(message); // Sending our message object to user
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }
}
