package com.f3rnandomoreno.telegramtaskaiagent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.f3rnandomoreno.telegramtaskaiagent.CommonTestIT;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.TaskRepository;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskStatus;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class TaskServiceTestIT extends CommonTestIT {

  @Autowired TaskRepository taskRepository;

  @Autowired TaskService taskService;

  @BeforeEach
  void setUp() {
    // Clear the database before each test
    taskRepository.deleteAll();
  }

  @Test
  void testExecuteSQLQueryInsert() {
    // given
    String insertQuery =
            "INSERT INTO tasks (description, assignee, status, created_at, updated_at) VALUES ('Read a book', 'John', 'TODO', '2023-04-15 10:00:00', '2023-04-15 10:00:00')";

    // when
    String result = taskService.executeSQLQuery(insertQuery);

    // then
    assertThat(result).isNotBlank();
    assertThat(result).contains("Inserción realizada con éxito");
    assertThat(result).contains("Filas afectadas: 1");
    assertThat(taskRepository.count()).isEqualTo(1);
  }

  @Test
  void testExecuteSQLQuerySelect() {
    // given
    TaskEntity task = new TaskEntity();
    task.setDescription("Read a book");
    task.setAssignee("John");
    task.setStatus(TaskStatus.TODO);
    taskRepository.save(task);

    String selectQuery = "SELECT * FROM tasks";

    // when
    String result = taskService.executeSQLQuery(selectQuery);

    // then
    assertThat(result).contains("Resultados:");
    assertThat(result).contains("Read a book");
    assertThat(result).contains("John");
    assertThat(result).contains("TODO");
  }

  @Test
  void testExecuteSQLQueryUpdate() {
    // given
    TaskEntity task = new TaskEntity();
    task.setDescription("Read a book");
    task.setAssignee("John");
    task.setStatus(TaskStatus.TODO);
    task = taskRepository.save(task);

    String updateQuery = "UPDATE tasks SET assignee = 'Jane' WHERE id = " + task.getId();

    // when
    String result = taskService.executeSQLQuery(updateQuery);

    // then
    assertThat(result).contains("Actualización realizada con éxito");
    assertThat(result).contains("Filas afectadas: 1");
    TaskEntity updatedTask = taskRepository.findById(task.getId()).orElseThrow();
    assertThat(updatedTask.getAssignee()).isEqualTo("Jane");
  }

  @Test
  void testExecuteSQLQueryDelete() {
    // given
    TaskEntity task = new TaskEntity();
    task.setDescription("Read a book");
    task.setAssignee("John");
    task.setStatus(TaskStatus.TODO);
    task = taskRepository.save(task);

    String deleteQuery = "DELETE FROM tasks WHERE id = " + task.getId();

    // when
    String result = taskService.executeSQLQuery(deleteQuery);

    // then
    assertThat(result).contains("Eliminación realizada con éxito");
    assertThat(result).contains("Filas afectadas: 1");
    assertThat(taskRepository.count()).isZero();
  }


  // Test to check if the "ver todas las tareas" command is triggered after an action
  @Test
  void testTriggerVerTodasLasTareasAfterAction() {
    // given
    String insertQuery =
        "INSERT INTO tasks (description, assignee, status, created_at, updated_at) VALUES ('leer un libro', 'María', 'TODO', '2023-04-15 10:00:00', '2023-04-15 10:00:00')";
    String updateQuery =
        "UPDATE tasks SET description = 'leer un libro actualizado' WHERE description = 'leer un libro'";
    String deleteQuery = "DELETE FROM tasks WHERE description = 'leer un libro actualizado'";

    // when
    String insertResult = taskService.executeSQLQuery(insertQuery);
    String updateResult = taskService.executeSQLQuery(updateQuery);
    String deleteResult = taskService.executeSQLQuery(deleteQuery);

    // then
    assertThat(insertResult).contains("Inserción realizada con éxito");
    assertThat(insertResult).contains("Filas afectadas: 1");
    assertThat(updateResult).contains("Actualización realizada con éxito");
    assertThat(updateResult).contains("Filas afectadas: 1");
    assertThat(deleteResult).contains("Eliminación realizada con éxito");
    assertThat(deleteResult).contains("Filas afectadas: 1");
  }

  @Test
  void testExecuteSQLQueryInvalidSyntax() {
    // given
    String invalidQuery = "INVALID SQL QUERY";

    // when & then
    assertThrows(RuntimeException.class, () -> taskService.executeSQLQuery(invalidQuery));
  }

  @Test
  void testExecuteSQLQueryNonExistentTable() {
    // given
    String nonExistentTableQuery = "SELECT * FROM non_existent_table";

    // when & then
    assertThrows(RuntimeException.class, () -> taskService.executeSQLQuery(nonExistentTableQuery));
  }
}
