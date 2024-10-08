package com.fmoreno.telegramtaskaiagent.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.fmoreno.telegramtaskaiagent.CommonTestIT;
import com.fmoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import com.fmoreno.telegramtaskaiagent.persistence.model.TaskStatus;
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

    TaskEntity savedTask = taskRepository.save(task);

    assertNotNull(savedTask.getId());
    Assertions.assertThat(savedTask.getDescription()).isEqualTo("This is a test task.");

    Optional<TaskEntity> retrievedTask = taskRepository.findById(savedTask.getId());
    assertTrue(retrievedTask.isPresent());
    assertEquals(savedTask.getId(), retrievedTask.get().getId());
    assertEquals("This is a test task.", retrievedTask.get().getDescription());
  }
}
