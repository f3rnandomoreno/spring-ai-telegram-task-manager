package com.f3rnandomoreno.telegramtaskaiagent.persistence;

import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity,Long> {}
