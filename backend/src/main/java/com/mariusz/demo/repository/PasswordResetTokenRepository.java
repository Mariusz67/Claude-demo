package com.mariusz.demo.repository;

import com.mariusz.demo.model.PasswordResetToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserEmail(String userEmail);

    @Modifying
    @Query("DELETE FROM password_reset_tokens WHERE expires_at < :now")
    void deleteExpired(LocalDateTime now);
}
