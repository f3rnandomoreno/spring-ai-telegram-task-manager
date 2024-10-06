package com.fmoreno.telegramtaskaiagent.client;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Log4j2
public class TelegramClientConfig implements LongPollingSingleThreadUpdateConsumer {

  final TelegramClient telegramClient;

  public TelegramClientConfig(String botToken) {
    telegramClient = new OkHttpTelegramClient(botToken);
  }

  @Override
  public void consume(Update update) {
    // We check if the update has a message and the message has text
    if (update.hasMessage() && update.getMessage().hasText()) {
      log.info("Received message: {}", update.getMessage().getText());
      // Set variables
      String message_text = update.getMessage().getText();
      long chat_id = update.getMessage().getChatId();

      SendMessage message =
          SendMessage // Create a message object
              .builder()
              .chatId(chat_id)
              .text(message_text)
              .build();
      try {
        telegramClient.execute(message); // Sending our message object to user
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    }
  }

}
