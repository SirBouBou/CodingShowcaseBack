package org.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.project.auth.model.RefreshToken;
import org.project.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationAccessMs}")
    private int jwtExpirationAccessMs;

    @Value("${app.jwtExpirationRefreshMs}")
    private int jwtExpirationRefreshMs;

    @Value("${app.jwtAccessCookieName}")
    private String jwtAccessCookie;

    @Value("${app.jwtRefreshCookieName}")
    private String jwtRefreshCookie;

    @Value("${app.cookie.secure")
    private boolean cookieSecure;

    public String getAccessCookie(HttpServletRequest request) {
        return getJwtFromCookies(request, jwtAccessCookie);
    }

    public String getRefreshCookie(HttpServletRequest request) {
        return getJwtFromCookies(request, jwtRefreshCookie);
    }
    public String getJwtFromCookies(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateAccessJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenForUser(userPrincipal.getId(), jwtExpirationAccessMs);
        return ResponseCookie.from(jwtAccessCookie, jwt).path("/").maxAge(jwtExpirationAccessMs / 1000).httpOnly(true).secure(cookieSecure).sameSite("Lax").build(); //TODO : Check if sameSite should be Lax
    }

    public ResponseCookie generateRefreshJwtCookie(RefreshToken token) {
        return ResponseCookie.from(jwtRefreshCookie, token.getToken()).path("/api/auth").maxAge(jwtExpirationRefreshMs / 1000).httpOnly(true).secure(cookieSecure).sameSite("Lax").build();
    }

    public ResponseCookie generateGuestJwtCookie(String token, int expiration) {
        return ResponseCookie.from(jwtAccessCookie, token).path("/").maxAge(expiration / 1000).httpOnly(true).secure(cookieSecure).sameSite("Lax").build();
    }

    public ResponseCookie getCleanJwtAccessCookie() {
        return ResponseCookie.from(jwtAccessCookie, "").path("/").maxAge(0).build();
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return ResponseCookie.from(jwtRefreshCookie, "").path("/api/auth").maxAge(0).build();
    }

    public String getSubjectFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getTypeFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public String generateToken(String subject, int expiration, String type) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date((now).getTime() + expiration))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenForUser(Long id, int expiration) {
        return generateToken(id.toString(), expiration, "USER");
    }

    public String generateTokenForGuest(UUID guestId, int expiration) {
        return generateToken(guestId.toString(), expiration, "GUEST");
    }
}
