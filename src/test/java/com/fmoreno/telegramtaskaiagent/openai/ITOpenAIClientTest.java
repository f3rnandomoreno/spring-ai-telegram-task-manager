package com.fmoreno.telegramtaskaiagent.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.ITCommonTest;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class ITOpenAIClientTest extends ITCommonTest {

  @Autowired
  ChatClient chatClient;

  // Test to check connection to OpenAI API
  @Test
  void testChatClient() {
    Prompt prompt = new Prompt("hola");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from openai: {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).isNotEmpty();
    assertThat(content).contains("Hola");
  }

  // Test to check if the "crearTarea" function is invoked correctly
  @Test
  void testCrearTareaPrompt() {
    Prompt prompt = new Prompt("Crea una tarea para jugar al padel mañana");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from openai (crearTarea): {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).containsIgnoringCase("Tarea creada");
  }

  // Test to check if the "modificarTarea" function is invoked correctly
  @Test
  void testModificarTareaPrompt() {
    Prompt prompt = new Prompt("Modifica la tarea con id 1 a completada");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from openai (modificarTarea): {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).containsIgnoringCase("Tarea modificada");
  }

  // Test to check if the "eliminarTarea" function is invoked correctly
  @Test
  void testEliminarTareaPrompt() {
    Prompt prompt = new Prompt("Elimina la tarea con id 1");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from openai (eliminarTarea): {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).containsIgnoringCase("Tarea eliminada");
  }

  // Test to check if the "verTareas" function is invoked correctly
  @Test
  void testVerTareasPrompt() {
    Prompt prompt = new Prompt("Muestra las tareas pendientes");
    log.info("Prompt: {}", prompt);
    String content = chatClient.prompt(prompt).call().content();
    log.info("Content from openai (verTareas): {}", content);
    assertThat(content).isNotBlank();
    assertThat(content).containsIgnoringCase("Tareas visualizadas");
  }
}