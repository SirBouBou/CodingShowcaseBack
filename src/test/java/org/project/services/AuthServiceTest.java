package org.project.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.enums.RegisterResult;
import org.project.models.*;
import org.project.payload.request.LoginRequest;
import org.project.payload.request.SignupRequest;
import org.project.payload.response.MessageResponse;
import org.project.payload.response.SigninResponse;
import org.project.payload.response.UserInfoResponse;
import org.project.repository.ProfileRepository;
import org.project.repository.RefreshTokenRepository;
import org.project.repository.RoleRepository;
import org.project.repository.UserRepository;
import org.project.security.jwt.JwtUtils;
import org.project.security.services.JwtRefreshTokenService;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
