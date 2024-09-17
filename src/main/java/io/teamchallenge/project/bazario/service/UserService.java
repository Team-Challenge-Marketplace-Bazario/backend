package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    Optional<User> findByEmail(String email);

    Optional<User> save(User user);
}
