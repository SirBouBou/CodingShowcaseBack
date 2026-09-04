package org.project.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.project.auth.model.RefreshToken;
import org.project.auth.model.User;
import org.project.auth.repository.RefreshTokenRepository;
import org.project.security.services.JwtRefreshTokenService;
import org.project.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilsTests {
    @Autowired
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtRefreshTokenService jwtRefreshTokenService;
    private User user;
    @Value("${app.jwtExpirationAccessMs}")
    private int jwtExpirationAccessMs;

    @Value("${app.jwtExpirationRefreshMs}")
    private int jwtExpirationRefreshMs;

    @BeforeEach
    void setUp() {
        this.jwtRefreshTokenService = new JwtRefreshTokenService(refreshTokenRepository);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        this.user = new User("testUsername", "test@email.com", "testPassword");
    }

    @Test
    void shouldGenerateAccessCookieFromUser() {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        //Création du cookie :
        ResponseCookie cookie = jwtUtils.generateAccessJwtCookie(userDetails);
        assertEquals("La durée de vie doit correspondre à 1/1000ème de la durée des properties", (long) jwtExpirationAccessMs/1000, cookie.getMaxAge().getSeconds());
        assertEquals("L'username doit pouvoir être récupéré depuis le cookie", "testUsername", jwtUtils.getSubjectFromJwtToken(cookie.getValue()));

        //Suppression du cookie :
        cookie = jwtUtils.getCleanJwtAccessCookie();
        assertEquals("La durée de vie doit avoir été fixée a 0 pour effacer le token", 0L, cookie.getMaxAge().getSeconds());
        assertEquals("Le token doit avoir été supprimée", "", cookie.getValue());
    }

    @Test
    void shouldGenerateRefreshCookieFromUser() {
        RefreshToken refreshToken = jwtRefreshTokenService.createRefreshToken(user);

        //Création du cookie :
        ResponseCookie cookie = jwtUtils.generateRefreshJwtCookie(refreshToken);
        assertEquals("La durée de vie doit correspondre à 1/1000ème de la durée des properties", (long) jwtExpirationRefreshMs/1000, cookie.getMaxAge().getSeconds());
        assertEquals("Le token du cookie doit correspondre au refresh token", refreshToken.getToken(), cookie.getValue());

        //Suppression du cookie :
        cookie = jwtUtils.getCleanJwtRefreshCookie();
        assertEquals("La durée de vie doit avoir été fixée a 0 pour effacer le token", 0L, cookie.getMaxAge().getSeconds());
        assertEquals("Le token doit avoir été supprimée", "", cookie.getValue());
    }
}