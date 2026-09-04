package org.project.security.jwt;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.project.security.services.GuestUserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import org.project.security.services.UserDetailsServiceImpl;

public class AuthTokenFilter extends OncePerRequestFilter {
    private JwtUtils jwtUtils;

    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/api/auth/signin")
                || path.equals("/api/auth/signup")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/signout")
                || path.startsWith("/api/test/all")
                || path.startsWith("/api/game")
                || path.startsWith("/api/showcase")
                || path.startsWith("/api/website")
                || path.equals("/api/guest/session");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = jwtUtils.getAccessCookie(request);
            if (jwt != null && !jwt.isBlank() && jwtUtils.validateJwtToken(jwt)) {
                String type = jwtUtils.getTypeFromJwtToken(jwt);
                String subject = jwtUtils.getSubjectFromJwtToken(jwt);
                UserDetails userDetails;

                if ("USER".equals(type)) {
                    Long userId = Long.parseLong(jwtUtils.getSubjectFromJwtToken(jwt));
                    userDetails =
                            userDetailsService.loadUserById(userId);
                } else if ("GUEST".equals(type)) {
                    UUID guestId = UUID.fromString(subject);
                    userDetails = new GuestUserDetailsImpl(guestId);
                } else {
                    throw new IllegalArgumentException((
                            "Unknown JWT type : " + type
                            ));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }
}
