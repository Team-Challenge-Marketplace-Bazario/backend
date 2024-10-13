package io.teamchallenge.project.bazario.repository;

import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordVerificationToken(String token);

    Optional<User> findByRefreshTokenToken(String token);
}
