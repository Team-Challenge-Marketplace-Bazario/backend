package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.*;

import java.util.Optional;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest request);

    Optional<LoginResponse> refreshToken(RefreshTokenRequest request);

    boolean logout(User user);

    void verifyEmail(VerifyEmailRequest request);

    void sendVerifyEmail(UsernameRequest request);

    void restorePassword(VerifyPasswordRequest request);

    void sendRestorePasswordEmail(UsernameRequest request);
}
