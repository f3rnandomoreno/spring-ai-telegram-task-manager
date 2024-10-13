package com.fmoreno.telegramtaskaiagent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.persistence.TaskRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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
        String sqlQuery = "INSERT INTO tasks (description, assignee) VALUES ('play padel', 'tomorrow')";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (createTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Inserción realizada con éxito");

        // Check the database
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(1L);
        assertThat(taskEntityOptional).isPresent();
        assertThat(taskEntityOptional.get().getDescription()).isEqualTo("play padel");
        assertThat(taskEntityOptional.get().getAssignee()).isEqualTo("tomorrow");
    }

    // Test to check if the "viewTasks" function is invoked correctly
    @Test
    void testViewTasksPrompt() {
        // given
        String sqlQuery = "SELECT * FROM tasks WHERE status = 'pending'";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (viewTasks): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Resultados:");
    }

    // Test to check if the "modifyTask" function is invoked correctly
    @Test
    void testModifyTaskPrompt() {
        // given
        String sqlQuery = "UPDATE tasks SET status = 'completed' WHERE id = 1";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (modifyTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Actualización realizada con éxito");

        // Check the database
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(1L);
        assertThat(taskEntityOptional).isPresent();
        assertThat(taskEntityOptional.get().getStatus().toString()).isEqualTo("completed");
    }

    // Test to check if the "deleteTask" function is invoked correctly
    @Test
    void testDeleteTaskPrompt() {
        // given
        String sqlQuery = "DELETE FROM tasks WHERE id = 1";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (deleteTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Eliminación realizada con éxito");

        // Check the database
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(1L);
        assertThat(taskEntityOptional).isNotPresent();
    }

    // Test to check if a single task can be fetched and displayed correctly
    @Test
    void testViewSingleTaskPrompt() {
        // given
        String sqlQuery = "SELECT * FROM tasks WHERE id = 1";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (viewSingleTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Resultados:");
    }

    // Test to check if the "createTask" function handles errors correctly
    @Test
    void testCreateTaskErrorHandling() {
        // given
        String sqlQuery = "INSERT INTO tasks (invalid_column) VALUES ('invalid_value')";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (createTaskErrorHandling): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Error al ejecutar la consulta");
    }

    // Test to check if the "viewTasks" function provides personalized responses
    @Test
    void testViewTasksPersonalizedResponse() {
        // given
        String sqlQuery = "SELECT * FROM tasks WHERE assignee = 'Fernando'";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (viewTasksPersonalizedResponse): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Fernando");
    }

    // Test to check if the "createTask" function handles missing fields
    @Test
    void testCreateTaskWithMissingFields() {
        // given
        String sqlQuery = "INSERT INTO tasks (description) VALUES ('play padel')";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (createTaskWithMissingFields): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Inserción realizada con éxito");
    }

    // Test to check if the "modifyTask" function handles non-existent tasks
    @Test
    void testModifyNonExistentTask() {
        // given
        String sqlQuery = "UPDATE tasks SET status = 'completed' WHERE id = 999";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (modifyNonExistentTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Actualización realizada con éxito");
    }

    // Test to check if the "deleteTask" function handles already deleted tasks
    @Test
    void testDeleteAlreadyDeletedTask() {
        // given
        String sqlQuery = "DELETE FROM tasks WHERE id = 999";
        String executionResult = taskService.executeSQLQuery(sqlQuery);
        log.info("Execution Result (deleteAlreadyDeletedTask): {}", executionResult);
        assertThat(executionResult).isNotBlank();
        assertThat(executionResult).containsIgnoringCase("Eliminación realizada con éxito");
    }
}
