package com.f3rnandomoreno.telegramtaskaiagent.persistence;

import com.f3rnandomoreno.telegramtaskaiagent.persistence.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(Long userId);
    Optional<UserEntity> findByEmail(String email);
}
