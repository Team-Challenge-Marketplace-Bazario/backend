package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.RefreshToken;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final Long refreshTokenDurationSeconds;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${app.jwt_refresh_duration_s}") Long refreshTokenDurationSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDurationSeconds = refreshTokenDurationSeconds;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken create(User user) {
        return refreshTokenRepository.save(new RefreshToken(
                null,
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(refreshTokenDurationSeconds),
                user));
    }

    @Override
    @Transactional
    public boolean verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public long deleteByUser(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }
}
