package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.RefreshToken;
import io.teamchallenge.project.bazario.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken create(User user);

    Optional<RefreshToken> verifyExpiration(RefreshToken refreshToken);

//    int delete(User user);
}
