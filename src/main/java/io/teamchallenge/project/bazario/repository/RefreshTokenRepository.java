package io.teamchallenge.project.bazario.repository;

import io.teamchallenge.project.bazario.entity.RefreshToken;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    long deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}
