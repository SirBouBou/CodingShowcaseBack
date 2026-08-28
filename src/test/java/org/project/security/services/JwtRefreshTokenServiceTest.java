package org.project.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.auth.model.RefreshToken;
import org.project.auth.model.User;
import org.project.auth.repository.RefreshTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
class JwtRefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    private JwtRefreshTokenService jwtRefreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        this.jwtRefreshTokenService = new JwtRefreshTokenService(refreshTokenRepository);

        ReflectionTestUtils.setField(
                jwtRefreshTokenService,
                "refreshExpiration",
                604800000
        );

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        this.user = new User("testUsername", "test@email.com", "testPassword");
    }

    @Test
    void shouldGenerateNewValidToken() {
        RefreshToken refreshToken = jwtRefreshTokenService.createRefreshToken(user);
        assertEquals("Un token fraichement crée doit être valable", Optional.of(refreshToken), jwtRefreshTokenService.verifyExpiration(refreshToken));
    }

    @Test
    void shouldExpireWhenRevoked() {
        RefreshToken refreshToken = jwtRefreshTokenService.createRefreshToken(user);
        jwtRefreshTokenService.revokeToken(refreshToken);
        assertEquals("Un token valable mais révoqué doit être invalide", Optional.empty(), jwtRefreshTokenService.verifyExpiration(refreshToken));
    }
    @Test
    void shouldExpireWhenTimesUp() {
        RefreshToken refreshToken = jwtRefreshTokenService.createRefreshToken(user);
        refreshToken.setExpiring(Instant.now().minusSeconds(10));
        assertEquals("Un token non révoqué mais expiré doit être invalide", Optional.empty(), jwtRefreshTokenService.verifyExpiration(refreshToken));
    }

}