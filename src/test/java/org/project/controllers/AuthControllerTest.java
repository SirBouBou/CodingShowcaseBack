package org.project.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.project.enums.RegisterResult;
import org.project.payload.request.LoginRequest;
import org.project.payload.request.SignupRequest;
import org.project.payload.response.MessageResponse;
import org.project.payload.response.SigninResponse;
import org.project.payload.response.SignoutResponse;
import org.project.payload.response.UserInfoResponse;
import org.project.security.jwt.JwtUtils;
import org.project.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthService authService;

    @Test
    void signin_shouldReturnUserInformation() throws Exception {

        UserInfoResponse userInfo =
                new UserInfoResponse(
                        1L,
                        "bob",
                        "bob@test.com",
                        List.of("ROLE_USER"),
                        null
                );

        ResponseCookie accessCookie =
                ResponseCookie.from("access", "access-jwt").build();

        ResponseCookie refreshCookie =
                ResponseCookie.from("refresh", "refresh-jwt").build();

        SigninResponse result = new SigninResponse(accessCookie.toString(), refreshCookie.toString(), userInfo);

        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(result);


        String json = """
                {
                    "username": "bob",
                    "password": "password"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authService)
                .authenticateUser(any(LoginRequest.class));
    }

    @Test
    void signup_shouldReturnSuccessMessage() throws Exception {
        when(authService.registerUser(any(SignupRequest.class)))
                .thenReturn(RegisterResult.SUCCESS);

        String json = """
                {
                    "username": "bob",
                    "email": "bob@email.com",
                    "password": "password"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully !"));

        verify(authService)
                .registerUser(any(SignupRequest.class));
    }

    @Test
    void signout_shouldReturnCleanCookieAndMessage() throws Exception {
        SignoutResponse result = new SignoutResponse(ResponseCookie.from("access", "").build().toString(), ResponseCookie.from("refresh", "").build().toString());

        when(jwtUtils.getRefreshCookie(any(HttpServletRequest.class)))
                .thenReturn("refresh-token");

        when(authService.logoutUser(anyString()))
                .thenReturn(result);

        mockMvc.perform(
                post("/api/auth/signout")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You've been signed out!"));

        verify(authService)
                .logoutUser("refresh-token");
    }

    @Test
    void refresh_shouldReturnCookieAndMessage() throws Exception {
        String result = ResponseCookie.from("access", "access-token").build().toString();

        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn("refresh-token");

        when(authService.refreshToken(anyString()))
                .thenReturn(Optional.of(result));

        mockMvc.perform(
                        post("/api/auth/refresh")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access token refreshed"));
        verify(authService)
                .refreshToken("refresh-token");
    }
}