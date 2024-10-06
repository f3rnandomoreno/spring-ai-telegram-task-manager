package com.fmoreno.telegramtaskaiagent.persistence;

import com.fmoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity,Long> {}
