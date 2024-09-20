package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.config.JwtTokenProvider;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.JwtException;
import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.LoginResponse;
import io.teamchallenge.project.bazario.web.dto.RefreshTokenRequest;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                           UserService userService, PasswordEncoder passwordEncoder,
                           RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var user = ((User) authentication.getPrincipal());

        final var accessToken = jwtTokenProvider.generateToken(user.getUsername());

        final var refreshToken = refreshTokenService.create(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        final var encodedPassword = passwordEncoder.encode(registerRequest.password());

        final var user = new User(null, registerRequest.firstName(), registerRequest.lastName(),
                registerRequest.email(), encodedPassword, registerRequest.phone());

        userService.save(user);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        final var token = request.refreshToken();

        final var refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!refreshTokenService.verifyExpiration(refreshToken)) {
            throw new JwtException("Refresh token expired");
        }

        final var user = refreshToken.getUser();

        final var accessToken = jwtTokenProvider.generateToken(user.getUsername());

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public boolean logout(User user) {
        return refreshTokenService.deleteByUser(user) > 0;
    }
}
