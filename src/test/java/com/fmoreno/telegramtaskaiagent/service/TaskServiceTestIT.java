package com.fmoreno.telegramtaskaiagent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.persistence.TaskRepository;
import com.fmoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import com.fmoreno.telegramtaskaiagent.persistence.model.TaskStatus;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
        String prompt = "Crea una nueva tarea: 'play padel tomorrow' asignada a Fernando";
        String sqlQuery = "INSERT INTO tasks (description, assignee, status) VALUES ('play padel tomorrow', 'Fernando', 'TODO')";

        // when
        taskService.executeSQLQuery(sqlQuery);

        // then
        List<TaskEntity> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        TaskEntity task = tasks.get(0);
        assertThat(task.getDescription()).isEqualTo("play padel tomorrow");
        assertThat(task.getAssignee()).isEqualTo("Fernando");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    // Test to check if the "viewTasks" function is invoked correctly
    @Test
    void testViewTasksPrompt() {
        // given
        String prompt = "Ver todas las tareas";
        String sqlQuery1 = "INSERT INTO tasks (description, assignee, status) VALUES ('task 1', 'Fernando', 'TODO')";
        String sqlQuery2 = "INSERT INTO tasks (description, assignee, status) VALUES ('task 2', 'Fernando', 'TODO')";

        taskService.executeSQLQuery(sqlQuery1);
        taskService.executeSQLQuery(sqlQuery2);

        // when
        List<TaskEntity> tasks = taskRepository.findAll();

        // then
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(TaskEntity::getDescription).containsExactlyInAnyOrder("task 1", "task 2");
    }

    // Test to check if the "modifyTask" function is invoked correctly
    @Test
    void testModifyTaskPrompt() {
        // given
        String prompt = "Modificar la tarea 'task to be modified' a 'modified task'";
        String sqlQuery = "INSERT INTO tasks (description, assignee, status) VALUES ('task to be modified', 'Fernando', 'TODO')";
        taskService.executeSQLQuery(sqlQuery);

        // when
        String updateQuery = "UPDATE tasks SET description = 'modified task' WHERE description = 'task to be modified'";
        taskService.executeSQLQuery(updateQuery);

        // then
        List<TaskEntity> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        TaskEntity task = tasks.get(0);
        assertThat(task.getDescription()).isEqualTo("modified task");
    }

    // Test to check if the "deleteTask" function is invoked correctly
    @Test
    void testDeleteTaskPrompt() {
        // given
        String prompt = "Eliminar la tarea 'task to be deleted'";
        String sqlQuery = "INSERT INTO tasks (description, assignee, status) VALUES ('task to be deleted', 'Fernando', 'TODO')";
        taskService.executeSQLQuery(sqlQuery);

        // when
        String deleteQuery = "DELETE FROM tasks WHERE description = 'task to be deleted'";
        taskService.executeSQLQuery(deleteQuery);

        // then
        List<TaskEntity> tasks = taskRepository.findAll();
        assertThat(tasks).isEmpty();
    }

    // Test to check if the "ver todas las tareas" command is triggered after an action
    @Test
    void testTriggerVerTodasLasTareasAfterAction() {
        // given
        String prompt = "Crea una nueva tarea: 'leer un libro' asignada a María";
        String sqlQuery = "INSERT INTO tasks (description, assignee, status) VALUES ('leer un libro', 'María', 'TODO')";
        taskService.executeSQLQuery(sqlQuery);

        // when
        String updateQuery = "UPDATE tasks SET description = 'leer un libro actualizado' WHERE description = 'leer un libro'";
        taskService.executeSQLQuery(updateQuery);

        String deleteQuery = "DELETE FROM tasks WHERE description = 'leer un libro actualizado'";
        taskService.executeSQLQuery(deleteQuery);

        // then
        List<TaskEntity> tasks = taskRepository.findAll();
        assertThat(tasks).isEmpty();
    }
}
