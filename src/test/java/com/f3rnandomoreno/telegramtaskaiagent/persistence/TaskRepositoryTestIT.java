package com.f3rnandomoreno.telegramtaskaiagent.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.f3rnandomoreno.telegramtaskaiagent.CommonTestIT;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskStatus;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskRepositoryTestIT extends CommonTestIT {

  @Autowired private TaskRepository taskRepository;

  @Test
  void testCreateTask() {
    TaskEntity task = new TaskEntity();
    task.setStatus(TaskStatus.TODO);
    task.setDescription("This is a test task.");
    task.setAssignee("Fernando");

    TaskEntity savedTask = taskRepository.save(task);

    assertNotNull(savedTask.getId());
    Assertions.assertThat(savedTask.getDescription()).isEqualTo("This is a test task.");
    Assertions.assertThat(savedTask.getAssignee()).isEqualTo("Fernando");

    Optional<TaskEntity> retrievedTask = taskRepository.findById(savedTask.getId());
    assertTrue(retrievedTask.isPresent());
    assertEquals(savedTask.getId(), retrievedTask.get().getId());
    assertEquals("This is a test task.", retrievedTask.get().getDescription());
    assertEquals("Fernando", retrievedTask.get().getAssignee());
  }

  @Test
  void testUpdateTaskAssignee() {
    TaskEntity task = new TaskEntity();
    task.setStatus(TaskStatus.TODO);
    task.setDescription("This is a test task.");
    task.setAssignee("Fernando");

    TaskEntity savedTask = taskRepository.save(task);
    savedTask.setAssignee("Maria");
    TaskEntity updatedTask = taskRepository.save(savedTask);

    assertNotNull(updatedTask.getId());
    Assertions.assertThat(updatedTask.getDescription()).isEqualTo("This is a test task.");
    Assertions.assertThat(updatedTask.getAssignee()).isEqualTo("Maria");

    Optional<TaskEntity> retrievedTask = taskRepository.findById(updatedTask.getId());
    assertTrue(retrievedTask.isPresent());
    assertEquals(updatedTask.getId(), retrievedTask.get().getId());
    assertEquals("This is a test task.", retrievedTask.get().getDescription());
    assertEquals("Maria", retrievedTask.get().getAssignee());
  }
}
