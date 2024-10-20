package com.fmoreno.telegramtaskaiagent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.persistence.TaskRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class TaskServiceTestIT extends CommonTestIT {

    @Autowired
    ChatClient chatClient;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskService taskService;

    // Test to check if the "createTask" function is invoked correctly
    @Test
    void testCreateTaskPrompt() {
        // given
        Prompt prompt = new Prompt("Create a task to play padel tomorrow");
        log.info("Prompt: {}", prompt);
        String content = chatClient.prompt(prompt).call().content();
        log.info("Content from OpenAI (createTask): {}", content);
        assertThat(content).isNotBlank();
        assertThat(content).containsIgnoringCase("Task created");
    }

    // Test to check if the "viewTasks" function is invoked correctly
    @Test
    void testViewTasksPrompt() {
        Prompt prompt = new Prompt("Show the pending tasks");
        log.info("Prompt: {}", prompt);
        String content = chatClient.prompt(prompt).call().content();
        log.info("Content from OpenAI (viewTasks): {}", content);
        assertThat(content).isNotBlank();
        assertThat(content).containsIgnoringCase("Tasks displayed");
    }

    // Test to check if the "modifyTask" function is invoked correctly
    @Test
    void testModifyTaskPrompt() {
        Prompt prompt = new Prompt("Modify the task with id 1 to completed");
        log.info("Prompt: {}", prompt);
        String content = chatClient.prompt(prompt).call().content();
        log.info("Content from OpenAI (modifyTask): {}", content);
        assertThat(content).isNotBlank();
        assertThat(content).containsIgnoringCase("Task modified");
    }

    // Test to check if the "deleteTask" function is invoked correctly
    @Test
    void testDeleteTaskPrompt() {
        Prompt prompt = new Prompt("Delete the task with id 1");
        log.info("Prompt: {}", prompt);
        String content = chatClient.prompt(prompt).call().content();
        log.info("Content from OpenAI (deleteTask): {}", content);
        assertThat(content).isNotBlank();
        assertThat(content).containsIgnoringCase("Task deleted");
    }

    // Test to check if the "ver todas las tareas" command is triggered after an action
    @Test
    void testTriggerVerTodasLasTareasAfterAction() {
        // given
        String insertQuery = "INSERT INTO tasks (description, assignee) VALUES ('leer un libro', 'María')";
        String updateQuery = "UPDATE tasks SET description = 'leer un libro actualizado' WHERE description = 'leer un libro'";
        String deleteQuery = "DELETE FROM tasks WHERE description = 'leer un libro actualizado'";

        // when
        String insertResult = taskService.executeSQLQuery(insertQuery);
        String updateResult = taskService.executeSQLQuery(updateQuery);
        String deleteResult = taskService.executeSQLQuery(deleteQuery);

        // then
        assertThat(insertResult).contains("Inserción realizada con éxito");
        assertThat(insertResult).contains("Resultados:");
        assertThat(updateResult).contains("Actualización realizada con éxito");
        assertThat(updateResult).contains("Resultados:");
        assertThat(deleteResult).contains("Eliminación realizada con éxito");
        assertThat(deleteResult).contains("Resultados:");
    }
}
