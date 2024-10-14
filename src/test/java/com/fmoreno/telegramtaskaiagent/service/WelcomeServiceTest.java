package com.fmoreno.telegramtaskaiagent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WelcomeServiceTest {

    private WelcomeService welcomeService;
    private TelegramClient telegramClient;

    @BeforeEach
    void setUp() {
        telegramClient = Mockito.mock(TelegramClient.class);
        welcomeService = new WelcomeService(telegramClient);
    }

    @Test
    void testShowStartMessage() throws TelegramApiException {
        Long chatId = 12345L;
        welcomeService.showStartMessage(chatId);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(argumentCaptor.capture());

        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(capturedMessage.getText()).contains("¡Bienvenido! Estas son tus opciones:");
    }

    @Test
    void testHandleVerTodasLasTareas() throws TelegramApiException {
        Long chatId = 12345L;
        welcomeService.handleVerTodasLasTareas(chatId);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(argumentCaptor.capture());

        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(capturedMessage.getText()).contains("Ejecutando comando: ver todas las tareas");
    }

    @Test
    void testHandleVerMisTareas() throws TelegramApiException {
        Long chatId = 12345L;
        welcomeService.handleVerMisTareas(chatId);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(argumentCaptor.capture());

        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(capturedMessage.getText()).contains("Ejecutando comando: ver mis tareas");
    }

    @Test
    void testShowStartMessageForVerifiedUser() throws TelegramApiException {
        Long chatId = 12345L;
        boolean isVerified = true;
        welcomeService.showStartMessage(chatId, isVerified);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(argumentCaptor.capture());

        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(capturedMessage.getText()).contains("¡Bienvenido! Estas son tus opciones:");
    }

    @Test
    void testShowStartMessageForUnverifiedUser() throws TelegramApiException {
        Long chatId = 12345L;
        boolean isVerified = false;
        welcomeService.showStartMessage(chatId, isVerified);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(argumentCaptor.capture());

        SendMessage capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(capturedMessage.getText()).contains("Hola! soy tu asistente de tareas, para verificar el acceso debes introducir tu correo electrónico");
    }
}
