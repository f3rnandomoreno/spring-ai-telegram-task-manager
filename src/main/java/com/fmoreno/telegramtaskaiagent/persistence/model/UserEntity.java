package com.fmoreno.telegramtaskaiagent.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;

  @Column(unique = true)
  private Long userId;

  private Long chatId;

  private String userName;
  private String firstName;
  private String lastName;
}
