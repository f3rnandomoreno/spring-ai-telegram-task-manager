package com.fmoreno.telegramtaskaiagent.persistence;

import com.fmoreno.telegramtaskaiagent.persistence.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(Long userId);
    Optional<UserEntity> findByEmail(String email);
}
