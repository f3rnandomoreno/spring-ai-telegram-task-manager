package com.f3rnandomoreno.telegramtaskaiagent.agents;

import com.f3rnandomoreno.telegramtaskaiagent.service.MessageService;
import com.f3rnandomoreno.telegramtaskaiagent.service.NotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotesAgent {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private NotesService notesService;

    public String chatAboutNote(String noteName, String userMessage) {
        String noteContent = notesService.getNoteContent(noteName);
        if (noteContent == null) {
            return "No se encontró la nota especificada.";
        }

        String prompt = String.format("""
            Contenido de la nota:
            %s
            
            Pregunta del usuario:
            %s
            
            Por favor, responde de forma concisa la pregunta basándote en el contenido de la nota.""",
            noteContent, userMessage);

        try {
            log.info("Procesando consulta con IA: {}", prompt);
            return chatClient.prompt(new Prompt(prompt)).call().content();
        } catch (Exception e) {
            log.error("Error al procesar la consulta con IA: {}", e.getMessage());
            return "Lo siento, hubo un error al procesar tu consulta.";
        }
    }
} 