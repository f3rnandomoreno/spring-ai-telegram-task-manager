package com.fmoreno.telegramtaskaiagent.persistence.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(name = "tasks")
public class TaskEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotEmpty private String description;
  private String assignee;

  @Enumerated(EnumType.STRING)
  private TaskStatus status = TaskStatus.TODO;

  private String lastUpdateByUser;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
}
