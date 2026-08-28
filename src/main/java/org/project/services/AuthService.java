package org.project.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.project.controllers.AuthController;
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
import org.project.security.jwt.JwtUtils;
import org.project.security.services.JwtRefreshTokenService;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    AuthenticationManager authenticationManager;

    UserRepository userRepository;

    ProfileRepository profileRepository;

    RoleRepository roleRepository;

    RefreshTokenRepository refreshTokenRepository;

    PasswordEncoder encoder;

    JwtUtils jwtUtils;

    UserDetailsServiceImpl userDetailsService;

    JwtRefreshTokenService jwtRefreshTokenService;

    @Value("${app.jwtAccessCookieName}")
    private String jwtAccessCookie;

    @Value("${app.jwtRefreshCookieName}")
    private String jwtRefreshCookie;


    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                          ProfileRepository profileRepository, RoleRepository roleRepository, PasswordEncoder encoder,
                          JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, JwtRefreshTokenService jwtRefreshTokenService,
                          RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.jwtRefreshTokenService = jwtRefreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public SigninResponse authenticateUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        jwtRefreshTokenService.revokeUserTokens(user);

        ResponseCookie jwtCookie = jwtUtils.generateAccessJwtCookie(userDetails);

        RefreshToken refreshToken = jwtRefreshTokenService.createRefreshToken(user);
        ResponseCookie refreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new SigninResponse(jwtCookie.toString(), refreshCookie.toString(), new UserInfoResponse(userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                userDetails.getProfile()
        ));
    }

    public RegisterResult registerUser(SignupRequest signUpRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
            return RegisterResult.USERNAME_ALREADY_EXISTS;
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
            return RegisterResult.EMAIL_ALREADY_EXISTS;
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        Profile profile = new Profile();
        if(signUpRequest.getIconId() == null) {
            profile.setIconId(0);
        } else {
            profile.setIconId(signUpRequest.getIconId());
        }

        profileRepository.save(profile);
        user.setProfile(profile);
        userRepository.save(user);

        return RegisterResult.SUCCESS;
    }

    public SignoutResponse logoutUser(String jwt) {
        if(jwt != null) {
            refreshTokenRepository.findByToken(jwt).ifPresent(jwtRefreshTokenService::revokeToken);
        }
        ResponseCookie deleteAccess = jwtUtils.getCleanJwtAccessCookie();
        ResponseCookie deleteRefresh = jwtUtils.getCleanJwtRefreshCookie();
        return new SignoutResponse(deleteAccess.toString(), deleteRefresh.toString());
    }

    public Optional<String> refreshToken(String jwt) {
        if(jwt == null) {
            return Optional.empty();
        }

        Optional<RefreshToken> refreshOpt = refreshTokenRepository.findByToken(jwt);
        if(refreshOpt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken refreshToken = refreshOpt.get();

        if (jwtRefreshTokenService.verifyExpiration(refreshToken).isEmpty()) {
            return Optional.empty();
        }

        User user = refreshToken.getUser();
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(user.getUsername());

        ResponseCookie accessCookie = jwtUtils.generateAccessJwtCookie(userDetails);

        logger.info("Access Token Refreshed");

        return Optional.of(accessCookie.toString());
    }
}
