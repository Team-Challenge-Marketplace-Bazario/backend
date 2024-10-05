package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.service.AuthService;
import io.teamchallenge.project.bazario.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-verify-email")
    public ResponseEntity<Void> sendVerifyEmail(@Valid UsernameRequest request) {
        authService.sendVerifyEmail(request);

        return ResponseEntity.ok().build();
    }

    //todo: post /api/auth/send-restore-password   - send email to restore password
    //todo: post /api/auth/verify-password  - get verification token with new password

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        final var deleted = authService.logout(user);

        return ResponseEntity
                .status(deleted ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .build();
    }
}
