package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.PasswordResetToken;
import com.shopflow.shopflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
