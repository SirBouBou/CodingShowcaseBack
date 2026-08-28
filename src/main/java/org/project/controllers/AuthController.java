package org.project.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.project.enums.RegisterResult;
import org.project.models.*;
import org.project.payload.request.LoginRequest;
import org.project.payload.request.SignupRequest;
import org.project.payload.response.MessageResponse;
import org.project.payload.response.SigninResponse;
import org.project.payload.response.SignoutResponse;
import org.project.payload.response.UserInfoResponse;
import org.project.repository.ProfileRepository;
import org.project.repository.RefreshTokenRepository;
import org.project.repository.RoleRepository;
import org.project.repository.UserRepository;
import org.project.security.jwt.AuthEntryPointJwt;
import org.project.security.jwt.JwtUtils;
import org.project.security.services.JwtRefreshTokenService;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
import org.project.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    AuthService authService;

    JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> signin(@Valid @RequestBody LoginRequest loginRequest) {
        SigninResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.accessCookie())
                .header(HttpHeaders.SET_COOKIE, response.refreshCookie())
                .body(response.userInfoResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest signUpRequest) {
        RegisterResult result = authService.registerUser(signUpRequest);
        return switch(result) {
            case SUCCESS ->
                ResponseEntity.ok(
                        new MessageResponse("User registered successfully !")
                );

            case USERNAME_ALREADY_EXISTS ->
                ResponseEntity.badRequest()
                        .body(new MessageResponse("Username is already taken !")
                );

            case EMAIL_ALREADY_EXISTS ->
                ResponseEntity.badRequest()
                        .body(new MessageResponse("Email is already taken !")
                );
        };
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> signout(HttpServletRequest request) {
        String jwt = jwtUtils.getRefreshCookie(request);
        SignoutResponse response = authService.logoutUser(jwt);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.AccessCookie())
                .header(HttpHeaders.SET_COOKIE, response.RefreshCookie())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refresh(HttpServletRequest request) {
        String jwt = jwtUtils.getRefreshCookie(request);
        Optional<String> result = authService.refreshToken(jwt);
        if(result.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE, result.get()
                )
                .body(
                        new MessageResponse("Access token refreshed")
                );
    }

}
