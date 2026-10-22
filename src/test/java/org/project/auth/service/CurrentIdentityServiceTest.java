package org.project.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.auth.exception.GuestNotFoundException;
import org.project.game.model.PlayerIdentity;
import org.project.game.model.PlayerId;
import org.project.game.model.PlayerType;
import org.project.security.services.GuestUserDetailsImpl;
import org.project.security.services.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentIdentityServiceTest {
    private AuthService authService;
    private GuestService guestService;
    private CurrentIdentityService service;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        guestService = mock(GuestService.class);
        service = new CurrentIdentityService(authService, guestService);
    }

    @Test
    void resolvesAuthenticatedUser() {
        UserDetailsImpl principal =
                new UserDetailsImpl(42L, "user", "user@example.com", "secret", java.util.List.of(), null);
        PlayerIdentity identity = new PlayerIdentity(new PlayerId(PlayerType.USER, "42"), "user");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        when(authService.getCurrentUser(42L)).thenReturn(identity);

        assertEquals(identity, service.getCurrentIdentity(authentication));
        verify(authService).getCurrentUser(42L);
    }

    @Test
    void resolvesAuthenticatedGuest() {
        UUID guestId = UUID.randomUUID();
        PlayerIdentity identity = new PlayerIdentity(new PlayerId(PlayerType.GUEST, guestId.toString()), "Guest");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(new GuestUserDetailsImpl(guestId), null);
        when(guestService.getCurrentGuest(guestId.toString())).thenReturn(identity);

        assertEquals(identity, service.getCurrentIdentity(authentication));
        verify(guestService).getCurrentGuest(guestId.toString());
    }

    @Test
    void propagatesMissingGuestAndRejectsUnsupportedPrincipal() {
        UUID guestId = UUID.randomUUID();
        Authentication guestAuthentication =
                new UsernamePasswordAuthenticationToken(new GuestUserDetailsImpl(guestId), null);
        when(guestService.getCurrentGuest(guestId.toString()))
                .thenThrow(new GuestNotFoundException("missing"));
        assertThrows(GuestNotFoundException.class, () -> service.getCurrentIdentity(guestAuthentication));

        Authentication anonymous = new UsernamePasswordAuthenticationToken("anonymous", null);
        assertThrows(AccessDeniedException.class, () -> service.getCurrentIdentity(anonymous));
    }
}
