package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.UpdateUserRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    User updateUser(User user, UpdateUserRequest updateUserRequest);

    User save(User user);

    void delete(User user);
}
