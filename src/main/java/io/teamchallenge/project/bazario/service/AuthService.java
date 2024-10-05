package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    boolean logout(User user);

    void verifyEmail(VerifyEmailRequest request);

    void sendVerifyEmail(UsernameRequest request);

    void restorePassword(VerifyPasswordRequest request);

    void sendRestorePasswordEmail(UsernameRequest request);
}
