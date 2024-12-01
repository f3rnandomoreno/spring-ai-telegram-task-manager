package com.f3rnandomoreno.telegramtaskaiagent.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class NotesService {

    private static final String NOTES_DIRECTORY = "notes/";
    private static final String NOTE_PREFIX = "note_";
    private static final String MD_EXTENSION = ".md";

    @PostConstruct
    public void init() {
        File directory = new File(NOTES_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void saveNote(String noteName, String content) {
        try {
            String fileName = buildFileName(noteName);
            File noteFile = new File(NOTES_DIRECTORY + fileName);
            FileWriter writer = new FileWriter(noteFile);
            writer.write(content);
            writer.close();
            log.info("Nota guardada: {}", fileName);
        } catch (IOException e) {
            log.error("Error al guardar la nota: {}", e.getMessage());
            throw new RuntimeException("Error al guardar la nota", e);
        }
    }

    public List<String> listNotes() {
        File directory = new File(NOTES_DIRECTORY);
        File[] files = directory.listFiles((dir, name) -> name.startsWith(NOTE_PREFIX) && name.endsWith(MD_EXTENSION));
        if (files == null) return new ArrayList<>();
        
        return Arrays.stream(files)
                .map(File::getName)
                .map(this::extractNoteName)
                .collect(Collectors.toList());
    }

    public String getNoteContent(String noteName) {
        try {
            String fileName = buildFileName(noteName);
            File noteFile = new File(NOTES_DIRECTORY + fileName);
            if (!noteFile.exists()) {
                return null;
            }
          return Files.readString(noteFile.toPath());
        } catch (IOException e) {
            log.error("Error al leer la nota: {}", e.getMessage());
            throw new RuntimeException("Error al leer la nota", e);
        }
    }

    private String buildFileName(String noteName) {
        return NOTE_PREFIX + noteName.toLowerCase().replaceAll("\\s+", "_") + MD_EXTENSION;
    }

    private String extractNoteName(String fileName) {
        return fileName.substring(NOTE_PREFIX.length(), fileName.length() - MD_EXTENSION.length());
    }

    private String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                  .replace("*", "\\*")
                  .replace("[", "\\[")
                  .replace("]", "\\]")
                  .replace("(", "\\(")
                  .replace(")", "\\)")
                  .replace("~", "\\~")
                  .replace("`", "\\`")
                  .replace(">", "\\>")
                  .replace("#", "\\#")
                  .replace("+", "\\+")
                  .replace("-", "\\-")
                  .replace("=", "\\=")
                  .replace("|", "\\|")
                  .replace("{", "\\{")
                  .replace("}", "\\}")
                  .replace(".", "\\.")
                  .replace("!", "\\!");
    }
} 