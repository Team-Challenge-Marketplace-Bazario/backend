package io.teamchallenge.project.bazario.config;

import io.teamchallenge.project.bazario.entity.Role;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.entity.UserRole;
import io.teamchallenge.project.bazario.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class AppInitializer {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String plainPassword;

    public AppInitializer(UserService userService,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.test_admin_password}") String plainPassword) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.plainPassword = plainPassword;
    }

    @PostConstruct
    @Transactional
    public void init() {
        log.info("========== Initializing application star ==========");

        userService.findByEmail("admin@server.com")
                        .ifPresent(userService::delete);

        final var password = passwordEncoder.encode(plainPassword);

        final var newUser = new User(null, "Henry", "Case", "admin@server.com", password, "+380000000000",
                true, null, null);

        newUser.setRoles(Set.of(
                new Role(null, UserRole.USER, newUser),
                new Role(null, UserRole.ADMIN, newUser)));

        final var storedUser = userService.save(newUser);

        log.info("admin user created: {}", storedUser);

        log.info("========== Initializing application end ==========");
    }
}
