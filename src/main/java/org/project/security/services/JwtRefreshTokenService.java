package org.project.security.services;

import org.project.auth.model.RefreshToken;
import org.project.auth.model.User;
import org.project.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class JwtRefreshTokenService {

    @Value("${app.jwtExpirationRefreshMs}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public JwtRefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public RefreshToken createRefreshToken(User user) {
        String rawToken = generateToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiring(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setToken(rawToken);
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> verifyExpiration(RefreshToken token) {
        if (token.getExpiring().isBefore(Instant.now()) || Boolean.TRUE.equals(token.getRevoked())) {
            refreshTokenRepository.delete(token);
            return Optional.empty();
        }
        return Optional.of(token);
    }

    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
