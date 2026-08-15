package org.project.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.project.models.*;
import org.project.payload.request.LoginRequest;
import org.project.payload.request.SignupRequest;
import org.project.payload.response.MessageResponse;
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
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
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

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles,
                        userDetails.getProfile()
                        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role admin is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role mod is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
                        roles.add(userRole);
                }
            });
        }

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

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser(HttpServletRequest request) {
        String jwt = jwtUtils.getRefreshCookie(request);
        if(jwt != null) {
            refreshTokenRepository.findByToken(jwt).ifPresent(jwtRefreshTokenService::revokeToken);
        }
        ResponseCookie deleteAccess = jwtUtils.getCleanJwtAccessCookie();
        ResponseCookie deleteRefresh = jwtUtils.getCleanJwtRefreshCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refreshToken(HttpServletRequest request) {
        String jwt = jwtUtils.getRefreshCookie(request);
        if(jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RefreshToken> refreshOpt = refreshTokenRepository.findByToken(jwt);
        if(refreshOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshToken refreshToken = refreshOpt.get();

        if (jwtRefreshTokenService.verifyExpiration(refreshToken).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = refreshToken.getUser();
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(user.getUsername());

        ResponseCookie accessCookie = jwtUtils.generateAccessJwtCookie(userDetails);

        System.out.println("Acces Token Refreshed"); //TODO: Change to logger
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(new MessageResponse("Access token refreshed"));
    }

}
