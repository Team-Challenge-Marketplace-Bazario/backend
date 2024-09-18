package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.config.JwtTokenProvider;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.LoginResponse;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                           UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var userDetails = ((UserDetails) authentication.getPrincipal());

        final var token = jwtTokenProvider.generateToken(userDetails.getUsername());

        return new LoginResponse(token, "");
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        final var encodedPassword = passwordEncoder.encode(registerRequest.password());

        final var user = new User(null, registerRequest.firstName(), registerRequest.lastName(),
                registerRequest.email(), encodedPassword, registerRequest.phone());

        userService.save(user);
    }
}
