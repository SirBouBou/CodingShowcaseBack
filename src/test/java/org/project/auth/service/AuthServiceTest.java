package org.project.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.auth.model.*;
import org.project.auth.service.AuthService;
import org.project.common.enums.RegisterResult;
import org.project.auth.dto.request.LoginRequest;
import org.project.auth.dto.request.SignupRequest;
import org.project.auth.dto.response.SigninResponse;
import org.project.auth.dto.response.SignoutResponse;
import org.project.auth.dto.response.UserInfoResponse;
import org.project.auth.repository.ProfileRepository;
import org.project.auth.repository.RefreshTokenRepository;
import org.project.auth.repository.RoleRepository;
import org.project.auth.repository.UserRepository;
import org.project.security.jwt.JwtUtils;
import org.project.security.services.JwtRefreshTokenService;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtRefreshTokenService jwtRefreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        User user = new User("testUsername", "testemail@email.com", "testPassword");
    }

    @Test
    void authenticate_shouldGetErrorIfUserDontExist() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUsernamediff", "testPassworddiff");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticateUser(loginRequest)
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    void authenticate_shouldAuthenticateValidUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUsername", "testPassword");

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("testUsername");
        when(userDetails.getEmail()).thenReturn("testemail@email.com");
        when(userDetails.getAuthorities()).thenReturn(List.of());
        when(userRepository.findById(userDetails.getId()))
                .thenReturn(Optional.of(new User("testUsername", "testemail@email.com", "encodedPassword")));
        when(jwtUtils.generateAccessJwtCookie(any(UserDetailsImpl.class)))
                .thenReturn(ResponseCookie.from("access", "access-token").build());
        when(jwtRefreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(new RefreshToken());
        when(jwtUtils.generateRefreshJwtCookie(any(RefreshToken.class)))
                .thenReturn(ResponseCookie.from("refresh", "refresh-token").build());

        UserInfoResponse validResult = new UserInfoResponse(1L, "testUsername", "testemail@email.com", List.of(), null);
        SigninResponse result = authService.authenticateUser(loginRequest);
        assertEquals("Le resultat doit contenir un access cookie", "access=access-token", result.accessCookie());
        assertEquals("Le resultat doit contenir un refresh cookie", "refresh=refresh-token", result.refreshCookie());
        assertEquals("Le resultat doit contenir les infos de l'utilisateurs", validResult , result.userInfoResponse());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtRefreshTokenService).revokeUserTokens(any(User.class));
    }

    @Test
    void register_shouldRejectExistingUsername() throws Exception {
        Mockito.when(userRepository.existsByUsername("testUsername"))
                .thenReturn(true);
        SignupRequest signupRequest = new SignupRequest("testUsername", "emaildiff@email.com", "passwordDiff", 0);
        RegisterResult result = authService.registerUser(signupRequest);
        assertEquals("Le resultat doit retourner Username already exist", RegisterResult.USERNAME_ALREADY_EXISTS, result);

        verify(userRepository).existsByUsername("testUsername");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void register_shouldRejectExistingEmail() throws Exception {
        Mockito.when(userRepository.existsByEmail("email@email.com"))
                .thenReturn(true);
        SignupRequest signupRequest = new SignupRequest("testUsernameDiff", "email@email.com", "passwordDiff", 0);
        RegisterResult result = authService.registerUser(signupRequest);
        assertEquals("Le resultat doit retourner Email already exist", RegisterResult.EMAIL_ALREADY_EXISTS, result);

        verify(userRepository).existsByEmail("email@email.com");
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void register_shouldRegisterCorrectUser() throws Exception {
        Mockito.when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(new Role(1, ERole.ROLE_USER)));
        SignupRequest signupRequest = new SignupRequest("testUsernameDiff", "emaildiff@email.com", "passwordDiff", 0);
        RegisterResult result = authService.registerUser(signupRequest);
        assertEquals("Le resultat doit retourner Success", RegisterResult.SUCCESS, result);

        verify(userRepository).existsByUsername("testUsernameDiff");
        verify(userRepository).existsByEmail("emaildiff@email.com");
        verify(encoder).encode("passwordDiff");
    }

    @Test
    void register_shouldRegisterCorrectUserEvenWithoutIcon() throws Exception {
        Mockito.when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(new Role(1, ERole.ROLE_USER)));
        SignupRequest signupRequest = new SignupRequest("testUsernameDiff", "emaildiff@email.com", "passwordDiff", null);
        RegisterResult result = authService.registerUser(signupRequest);
        assertEquals("Le resultat doit retourner Success", RegisterResult.SUCCESS, result);
    }

    @Test
    void register_shouldFailWhenUserRoleIsMissing() {
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        SignupRequest request = new SignupRequest("new-user", "new@example.com", "password", null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.registerUser(request));

        org.junit.jupiter.api.Assertions.assertEquals("Error: Role user is not found.", exception.getMessage());
        verify(profileRepository, never()).save(any(Profile.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void logoutClearsCookiesAndRevokesMatchingRefreshToken() {
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(token));
        when(jwtUtils.getCleanJwtAccessCookie()).thenReturn(ResponseCookie.from("access", "").build());
        when(jwtUtils.getCleanJwtRefreshCookie()).thenReturn(ResponseCookie.from("refresh", "").build());

        SignoutResponse response = authService.logoutUser("refresh-token");

        org.junit.jupiter.api.Assertions.assertEquals("access=", response.AccessCookie());
        org.junit.jupiter.api.Assertions.assertEquals("refresh=", response.RefreshCookie());
        verify(jwtRefreshTokenService).revokeToken(token);
    }

    @Test
    void logoutWithoutRefreshTokenDoesNotLookUpOrRevokeToken() {
        when(jwtUtils.getCleanJwtAccessCookie()).thenReturn(ResponseCookie.from("access", "").build());
        when(jwtUtils.getCleanJwtRefreshCookie()).thenReturn(ResponseCookie.from("refresh", "").build());

        authService.logoutUser(null);

        verifyNoInteractions(refreshTokenRepository, jwtRefreshTokenService);
    }

    @Test
    void refreshReturnsEmptyForMissingUnknownOrExpiredToken() {
        org.junit.jupiter.api.Assertions.assertTrue(authService.refreshToken(null).isEmpty());
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertTrue(authService.refreshToken("unknown").isEmpty());

        RefreshToken expired = new RefreshToken();
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));
        when(jwtRefreshTokenService.verifyExpiration(expired)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertTrue(authService.refreshToken("expired").isEmpty());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void refreshCreatesAccessCookieForValidRefreshToken() {
        User user = new User();
        user.setUsername("alice");
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        UserDetailsImpl details = mock(UserDetailsImpl.class);
        when(refreshTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(jwtRefreshTokenService.verifyExpiration(token)).thenReturn(Optional.of(token));
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(details);
        when(jwtUtils.generateAccessJwtCookie(details))
                .thenReturn(ResponseCookie.from("access", "new-access").build());

        Optional<String> result = authService.refreshToken("valid");

        org.junit.jupiter.api.Assertions.assertEquals(Optional.of("access=new-access"), result);
    }

    @Test
    void resolvesCurrentUserAndReportsMissingUser() {
        User user = new User();
        user.setId(9L);
        user.setUsername("alice");
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        org.junit.jupiter.api.Assertions.assertEquals("9", authService.getCurrentUser(9L).getId().value());
        org.junit.jupiter.api.Assertions.assertEquals("alice", authService.getCurrentUser(9L).getDisplayName());

        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(org.project.auth.exception.UserNotFoundException.class,
                () -> authService.getCurrentUser(10L));
    }

/*    @Test
    void signup_shouldRejectExistingEmail() throws Exception {

        when(userRepository.existsByEmail("bob@test.com"))
                .thenReturn(true);

        String json = """
            {
                "username": "bob",
                "email": "bob@test.com",
                "password": "password"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Error: Email is already in use!"));

        verify(userRepository)
                .existsByEmail("bob@test.com");

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void signup_shouldRegisterUser() throws Exception {

        when(userRepository.existsByUsername("bob"))
                .thenReturn(false);

        when(userRepository.existsByEmail("bob@test.com"))
                .thenReturn(false);

        when(encoder.encode("password"))
                .thenReturn("encodedPassword");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String json = """
            {
                "username": "bob",
                "email": "bob@test.com",
                "role": null,
                "password": "password"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully!"));

        verify(userRepository).existsByUsername("bob");
        verify(userRepository).existsByEmail("bob@test.com");
        verify(encoder).encode("password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("The username of the saved user should be the one from the request.", "bob", savedUser.getUsername());
        assertEquals("The email of the saved user should be the one from the request.", "bob@test.com", savedUser.getEmail());
        assertEquals("The password of the saved user should be the encoded version of the one from the request.", "encodedPassword", savedUser.getPassword());

        ArgumentCaptor<Profile> profileCaptor =
                ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        Profile savedProfile = profileCaptor.getValue();
        assertEquals("The profile iconId should be 0 if not present in request", 0, savedProfile.getIconId());
    }

    @Test
    void signup_shouldRegisterWithIconId() throws Exception {

        when(userRepository.existsByUsername("bob"))
                .thenReturn(false);

        when(userRepository.existsByEmail("bob@test.com"))
                .thenReturn(false);

        when(encoder.encode("password"))
                .thenReturn("encodedPassword");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String json = """
            {
                "username": "bob",
                "email": "bob@test.com",
                "password": "password",
                "iconId": 1
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully!"));

        verify(userRepository).existsByUsername("bob");

        ArgumentCaptor<Profile> profileCaptor =
                ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        Profile savedProfile = profileCaptor.getValue();
        assertEquals("The profile iconId should be set if set in request", 1, savedProfile.getIconId());
    }

    @Test
    void refresh_shouldReturn401WhenNoCookie() throws Exception {
        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn(null);

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());

        verify(refreshTokenRepository, never())
                .findByToken(any());
    }

    @Test
    void refresh_shouldReturn401WhenTokenNotFound()
            throws Exception {

        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldGenerateNewAccessCookie()
            throws Exception {

        String jwt = "refresh-token";

        User user = new User();
        user.setUsername("bob");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        ResponseCookie cookie =
                ResponseCookie.from("access", "new-jwt").build();

        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn(jwt);

        when(refreshTokenRepository.findByToken(jwt))
                .thenReturn(Optional.of(refreshToken));

        when(jwtRefreshTokenService.verifyExpiration(refreshToken))
                .thenReturn(Optional.of(refreshToken));

        when(userDetailsService.loadUserByUsername("bob"))
                .thenReturn(userDetails);

        when(jwtUtils.generateAccessJwtCookie(userDetails))
                .thenReturn(cookie);

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Access token refreshed"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("access=new-jwt")
                ));
    }

    @Test
    void signout_withoutRefreshToken_shouldClearCookies()
            throws Exception {

        ResponseCookie accessCookie =
                ResponseCookie.from("access", "").build();

        ResponseCookie refreshCookie =
                ResponseCookie.from("refresh", "").build();

        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn(null);

        when(jwtUtils.getCleanJwtAccessCookie())
                .thenReturn(accessCookie);

        when(jwtUtils.getCleanJwtRefreshCookie())
                .thenReturn(refreshCookie);

        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("You've been signed out!"));

        verify(refreshTokenRepository, never())
                .findByToken(any());
    }

    @Test
    void signout_withRefreshToken_shouldClearCookies()
            throws Exception {

        when(jwtUtils.getRefreshCookie(any()))
                .thenReturn("refresh-token");

        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.of(refreshToken));

        when(jwtUtils.getCleanJwtAccessCookie())
                .thenReturn(ResponseCookie.from("access", "").build());

        when(jwtUtils.getCleanJwtRefreshCookie())
                .thenReturn(ResponseCookie.from("refresh", "").build());

        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("You've been signed out!"));

        verify(refreshTokenRepository)
                .findByToken(any());
        verify(jwtRefreshTokenService).revokeToken(refreshToken);
    }*/
}
