package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.config.JwtTokenProvider;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.entity.Verification;
import io.teamchallenge.project.bazario.exceptions.IllegalOperationException;
import io.teamchallenge.project.bazario.exceptions.JwtException;
import io.teamchallenge.project.bazario.helpers.EMailHelper;
import io.teamchallenge.project.bazario.repository.UserRepository;
import io.teamchallenge.project.bazario.web.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EMailHelper eMailHelper;
    private final Long verificationDuration;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RefreshTokenService refreshTokenService,
                           EMailHelper eMailHelper,
                           @Value("${app.verification_duration_s}") Long verificationDuration) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.eMailHelper = eMailHelper;
        this.verificationDuration = verificationDuration;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        final var user = ((User) authentication.getPrincipal());
        if (!user.isVerified()) {
            throw new IllegalOperationException("User is not verified");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var accessToken = jwtTokenProvider.generateToken(user.getEmail());

        final var refreshToken = refreshTokenService.getOrCreate(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        final var registeredUser = userRepository.findByEmail(registerRequest.email());
        if (registeredUser.isPresent()) {
            log.error("User with email {} already exists", registerRequest.email());
            throw new IllegalOperationException("User with email " + registerRequest.email() + " already exists");
        }

        final var encodedPassword = passwordEncoder.encode(registerRequest.password());

        final var emailVerification = getVerification(verificationDuration);

        final var user = new User(null, registerRequest.firstName(), registerRequest.lastName(),
                registerRequest.email(), encodedPassword, registerRequest.phone(), false, emailVerification, null);

        final var savedUser = userRepository.save(user);

        eMailHelper.sendVerificationEmail(savedUser);
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
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        final var user = userRepository.findByEmailVerificationToken(request.token())
                .orElseThrow(() -> new RuntimeException(
                        String.format("User with verification token %s not found", request.token())));

        if (user.isVerified()) {
            throw new IllegalOperationException(
                    String.format("User %s is already verified", user)
            );
        }

        if (user.getEmailVerification().getExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalOperationException(
                    String.format("Email verification token has expired, due date is: %s",
                            user.getEmailVerification().getExpires())
            );
        }

        user.setEmailVerification(null);
        user.setVerified(true);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendVerifyEmail(UsernameRequest request) {
        final Function<String, Optional<User>> getUser = request.username().contains("@")
                ? userRepository::findByEmail
                : userRepository::findByPhone;

        final var user = getUser.apply(request.username())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User %s not found", request.username())));

        if (user.isVerified()) {
            log.warn("User with username: {} is already verified", user.getEmail());
            return;
        }

        if (user.getEmailVerification().getExpires().isAfter(LocalDateTime.now())) {
            log.warn("last letter duration expires at {}", user.getEmailVerification().getExpires());
            return;
        }

        user.setEmailVerification(getVerification(verificationDuration));
        final var updatedUser = userRepository.save(user);

        eMailHelper.sendVerificationEmail(updatedUser);
    }

    @Override
    public boolean logout(User user) {
        return refreshTokenService.deleteByUser(user) > 0;
    }

    private Verification getVerification(long verificationDuration) {
        final var byteToken = new byte[16];

        new SecureRandom().nextBytes(byteToken);
        final var stringToken = Base64.getUrlEncoder().encodeToString(byteToken);

        return new Verification(stringToken, LocalDateTime.now().plusSeconds(verificationDuration));
    }
}
