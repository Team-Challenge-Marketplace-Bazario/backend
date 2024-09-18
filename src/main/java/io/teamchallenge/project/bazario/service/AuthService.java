package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.LoginResponse;
import io.teamchallenge.project.bazario.web.dto.RefreshTokenRequest;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest request);

    LoginResponse refreshToken(@Valid RefreshTokenRequest request);
}
