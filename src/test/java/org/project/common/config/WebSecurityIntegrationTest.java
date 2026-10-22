package org.project.common.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
import org.project.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityIntegrationTest {
    private static final String ACCESS_COOKIE = "testAccess";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void allowsPublicEndpointButProtectsMethodSecuredEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public Content."));

        mockMvc.perform(get("/api/test/user"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesValidGuestCookieAndEnforcesRoles() throws Exception {
        String guestToken = jwtUtils.generateTokenForGuest(UUID.randomUUID(), 60_000);
        Cookie guestCookie = new Cookie(ACCESS_COOKIE, guestToken);

        mockMvc.perform(get("/api/test/user").cookie(guestCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("Any user or guest Content."));

        mockMvc.perform(get("/api/test/admin").cookie(guestCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizesUserRolesFromVerifiedUserToken() throws Exception {
        UserDetailsImpl admin = new UserDetailsImpl(
                17L,
                "admin",
                "admin@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                null
        );
        when(userDetailsService.loadUserById(17L)).thenReturn(admin);
        Cookie adminCookie = new Cookie(
                ACCESS_COOKIE,
                jwtUtils.generateTokenForUser(17L, 60_000)
        );

        mockMvc.perform(get("/api/test/user").cookie(adminCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/admin").cookie(adminCookie))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidAndExpiredAccessTokens() throws Exception {
        mockMvc.perform(get("/api/test/user").cookie(new Cookie(ACCESS_COOKIE, "invalid-token")))
                .andExpect(status().isUnauthorized());

        String expiredToken = jwtUtils.generateTokenForGuest(UUID.randomUUID(), -1);
        mockMvc.perform(get("/api/test/user").cookie(new Cookie(ACCESS_COOKIE, expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void appliesCorsPolicyToAllowedPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/test/user")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
