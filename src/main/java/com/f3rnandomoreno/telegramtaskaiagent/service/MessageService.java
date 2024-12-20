package com.f3rnandomoreno.telegramtaskaiagent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final TelegramClient telegramClient;

    public void sendMessage(Long chatId, String text) {
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
            e.printStackTrace();
        }
    }

    public void sendMessagesToMultipleUsers(List<Long> chatIds, String text) {
        for (Long chatId : chatIds) {
            sendMessage(chatId, text);
        }
    }
}
