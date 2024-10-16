package com.fmoreno.telegramtaskaiagent.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class WelcomeService {

    private final TelegramClient telegramClient;

    public WelcomeService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void showStartMessage(Long chatId) {
        String welcomeMessage = "¡Bienvenido! Estas son tus opciones:\n" +
                "/ver_todas_las_tareas - Ver todas las tareas\n" +
                "/ver_mis_tareas - Ver mis tareas";
        sendMessage(chatId, welcomeMessage);
    }

    public void handleVerTodasLasTareas(Long chatId) {
        String message = "Ejecutando comando: ver todas las tareas";
        sendMessage(chatId, message);
        // Aquí puedes agregar la lógica para manejar la opción de ver todas las tareas
    }

    public void handleVerMisTareas(Long chatId) {
        String message = "Ejecutando comando: ver mis tareas";
        sendMessage(chatId, message);
        // Aquí puedes agregar la lógica para manejar la opción de ver mis tareas
    }

    public void showStartMessage(Long chatId, boolean isVerified) {
        String welcomeMessage;
        if (isVerified) {
            welcomeMessage = "¡Bienvenido! Estas son tus opciones:\n" +
                    "/ver_todas_las_tareas - Ver todas las tareas\n" +
                    "/ver_mis_tareas - Ver mis tareas";
        } else {
            welcomeMessage = "Hola! soy tu asistente de tareas, para verificar el acceso debes introducir tu correo electrónico";
        }
        sendMessage(chatId, welcomeMessage);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
