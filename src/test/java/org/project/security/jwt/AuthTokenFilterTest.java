package org.project.security.jwt;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.project.security.services.GuestUserDetailsImpl;
import org.project.security.services.UserDetailsImpl;
import org.project.security.services.UserDetailsServiceImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTokenFilterTest {
    private final JwtUtils jwtUtils = mock(JwtUtils.class);
    private final UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
    private final AuthTokenFilter filter = new AuthTokenFilter(jwtUtils, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesUserTokenAndContinuesFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        UserDetailsImpl user = new UserDetailsImpl(5L, "alice", "alice@example.com", "secret", List.of(), null);
        when(jwtUtils.getAccessCookie(request)).thenReturn("token");
        when(jwtUtils.validateJwtToken("token")).thenReturn(true);
        when(jwtUtils.getTypeFromJwtToken("token")).thenReturn("USER");
        when(jwtUtils.getSubjectFromJwtToken("token")).thenReturn("5");
        when(userDetailsService.loadUserById(5L)).thenReturn(user);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertSame(user, authentication.getPrincipal());
        verify(chain).doFilter(request, response);
        verify(userDetailsService).loadUserById(5L);
    }

    @Test
    void authenticatesGuestToken() throws Exception {
        UUID guestId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtils.getAccessCookie(request)).thenReturn("token");
        when(jwtUtils.validateJwtToken("token")).thenReturn(true);
        when(jwtUtils.getTypeFromJwtToken("token")).thenReturn("GUEST");
        when(jwtUtils.getSubjectFromJwtToken("token")).thenReturn(guestId.toString());

        filter.doFilter(request, response, mock(FilterChain.class));

        assertInstanceOf(GuestUserDetailsImpl.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(guestId, ((GuestUserDetailsImpl)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getGuestId());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void leavesRequestUnauthenticatedWhenTokenIsMissingOrInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtUtils.getAccessCookie(request)).thenReturn(" ");

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtils, never()).validateJwtToken(anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void bypassesPublicAuthPathsAndOptionsRequests() throws Exception {
        for (String path : List.of(
                "/api/auth/signin", "/api/auth/signup", "/api/auth/refresh",
                "/api/auth/signout", "/api/test/all/content", "/api/showcase/all",
                "/api/website/games", "/api/guest/session"
        )) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
        }
        MockHttpServletRequest options = new MockHttpServletRequest("OPTIONS", "/api/private");
        filter.doFilter(options, new MockHttpServletResponse(), mock(FilterChain.class));

        verifyNoInteractions(jwtUtils);
    }

    @Test
    void ignoresMalformedAndUnknownTokenTypesButStillRunsChain() throws Exception {
        SecurityContextHolder.clearContext();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest malformedRequest = new MockHttpServletRequest("GET", "/api/private");
        when(jwtUtils.getAccessCookie(malformedRequest)).thenReturn("token");
        when(jwtUtils.validateJwtToken("token")).thenReturn(true);
        when(jwtUtils.getTypeFromJwtToken("token")).thenReturn("UNKNOWN");

        filter.doFilter(malformedRequest, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(eq(malformedRequest), any());
    }
}
