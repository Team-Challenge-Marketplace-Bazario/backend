package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.config.JwtTokenProvider;
import io.teamchallenge.project.bazario.entity.Role;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.entity.UserRole;
import io.teamchallenge.project.bazario.entity.Verification;
import io.teamchallenge.project.bazario.exceptions.IllegalOperationException;
import io.teamchallenge.project.bazario.exceptions.UserNotFoundException;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EMailHelper eMailHelper;

    private final Long verificationDuration;
    private final Long refreshTokenDurationSeconds;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EMailHelper eMailHelper,
                           @Value("${app.verification_duration_s}") Long verificationDuration,
                           @Value("${app.jwt_refresh_duration_s}") Long refreshTokenDurationSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eMailHelper = eMailHelper;
        this.verificationDuration = verificationDuration;
        this.refreshTokenDurationSeconds = refreshTokenDurationSeconds;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        final var user = ((User) authentication.getPrincipal());
        if (!user.isVerified()) {
            throw new IllegalOperationException("User is not verified");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var accessToken = jwtTokenProvider.generateToken(user.getEmail());

        final var refreshToken = updateOrCreate(user);
        user.setRefreshToken(refreshToken);

        return new LoginResponse(accessToken, user.getRefreshToken().getToken());
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

        user.setRoles(Set.of(new Role(null, UserRole.USER, user)));

        final var savedUser = userRepository.save(user);

        eMailHelper.sendVerificationEmail(savedUser);
    }

    @Override
    @Transactional
    public Optional<LoginResponse> refreshToken(RefreshTokenRequest request) {
        final var token = request.refreshToken();

        final var user = userRepository.findByRefreshTokenToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (isExpired(user.getRefreshToken())) {
            user.setRefreshToken(null);
        } else {
            user.getRefreshToken().setExpires(LocalDateTime.now().plusSeconds(refreshTokenDurationSeconds));
        }

        final var accessToken = jwtTokenProvider.generateToken(user.getUsername());

        return user.getRefreshToken() == null
                ? Optional.empty()
                : Optional.of(new LoginResponse(accessToken, user.getRefreshToken().getToken()));
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        final var user = userRepository.findByEmailVerificationToken(request.token())
                .orElseThrow(() -> new UserNotFoundException(
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
        final var getUser = getUser(request);

        final var user = getUser.apply(request.username())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User %s not found", request.username())));

        if (user.isVerified()) {
            throw new IllegalOperationException(
                    String.format("User with username: %s is already verified", user.getEmail()));
        }

        if (user.getEmailVerification().getExpires().isAfter(LocalDateTime.now())) {
            throw new IllegalOperationException(
                    String.format("last letter duration expires at %s", user.getEmailVerification().getExpires()));
        }

        user.setEmailVerification(getVerification(verificationDuration));
        final var updatedUser = userRepository.save(user);

        eMailHelper.sendVerificationEmail(updatedUser);
    }

    @Override
    @Transactional
    public void restorePassword(VerifyPasswordRequest request) {
        // find user
        final var user = userRepository.findByPasswordVerificationToken(request.token())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with password restore token %s not found", request.token())));

        // check active
        if (!user.isVerified()) {
            throw new IllegalOperationException(
                    String.format("User %s is not verified", user.getEmail())
            );
        }

        // check expire
        if (user.getPasswordVerification().getExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalOperationException(
                    String.format("Password verification token has expired, due date is: %s",
                            user.getPasswordVerification().getExpires()));
        }

        // remove token from db
        user.setPasswordVerification(null);
        user.setPassword(passwordEncoder.encode(request.password()));

        // update password and save
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendRestorePasswordEmail(UsernameRequest request) {
        // find user
        final var user = getUser(request).apply(request.username())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User %s not found", request.username())));

        // check if non active
        if (!user.isVerified()) {
            throw new IllegalOperationException(
                    String.format("User with username: %s is not verified", user.getEmail()));
        }

        // check if token present and expired
        if (user.getPasswordVerification() != null
            && user.getPasswordVerification().getExpires().isAfter(LocalDateTime.now())) {
            throw new IllegalOperationException(
                    String.format("last restore password letter duration expires at %s",
                            user.getPasswordVerification().getExpires())
            );
        }

        user.setPasswordVerification(getVerification(verificationDuration));
        final var updatedUser = userRepository.save(user);

        eMailHelper.sendRestorePasswordEmail(updatedUser);
    }

    @Override
    @Transactional
    public boolean logout(User user) {
        user.setRefreshToken(null);

        return true;
    }

    private Verification getVerification(long verificationDuration) {
        final var byteToken = new byte[32];

        new SecureRandom().nextBytes(byteToken);
        final var stringToken = Base64.getUrlEncoder().encodeToString(byteToken);

        return new Verification(stringToken, LocalDateTime.now().plusSeconds(verificationDuration));
    }

    private Function<String, Optional<User>> getUser(UsernameRequest request) {
        return request.username().contains("@")
                ? userRepository::findByEmail
                : userRepository::findByPhone;
    }

    private Verification updateOrCreate(User user) {
        final var storedToken = user.getRefreshToken();
        if (storedToken != null) {
            storedToken.setExpires(LocalDateTime.now().plusSeconds(refreshTokenDurationSeconds));

            return storedToken;
        } else {
            return new Verification(UUID.randomUUID().toString(),
                    LocalDateTime.now().plusSeconds(refreshTokenDurationSeconds));
        }
    }

    private boolean isExpired(Verification refreshToken) {
        return refreshToken.getExpires().isBefore(LocalDateTime.now());
    }

}
