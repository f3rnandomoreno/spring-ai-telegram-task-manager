package com.f3rnandomoreno.telegramtaskaiagent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WelcomeServiceTest {

    private MessageService messageService;

    private WelcomeService welcomeService;

    @BeforeEach
    void setUp() {
        messageService = Mockito.mock(MessageService.class);
        welcomeService = new WelcomeService(messageService);
    }

    @Test
    void testShowStartMessage() {
        Long chatId = 12345L;
        welcomeService.showStartMessage(chatId);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService).sendMessage(eq(chatId), argumentCaptor.capture());

        String capturedMessage = argumentCaptor.getValue();
        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage).contains("¡Bienvenido! Soy el agente de IA Moreno");
    }
}
