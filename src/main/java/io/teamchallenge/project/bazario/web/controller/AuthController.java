package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.IllegalOperationException;
import io.teamchallenge.project.bazario.service.AuthService;
import io.teamchallenge.project.bazario.web.dto.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        final var loginResponse = authService.login(request);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        final var response = authService.refreshToken(request);

        return ResponseEntity.of(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-verify-email")
    public ResponseEntity<Void> sendVerifyEmail(@Valid @RequestBody UsernameRequest request) {
        authService.sendVerifyEmail(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore-password")
    public ResponseEntity<Void> restorePassword(@Valid @RequestBody VerifyPasswordRequest request) {

        authService.restorePassword(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-restore-password")
    public ResponseEntity<Void> sendRestorePassword(@Valid @RequestBody UsernameRequest request) {

        authService.sendRestorePasswordEmail(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        final var deleted = authService.logout(user);

        return ResponseEntity
                .status(deleted ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler({IllegalOperationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Void> handleIllegalOperationException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
