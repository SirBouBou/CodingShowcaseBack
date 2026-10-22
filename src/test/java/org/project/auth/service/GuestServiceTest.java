package org.project.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.auth.dto.response.GuestSessionResponse;
import org.project.auth.exception.GuestNotFoundException;
import org.project.auth.exception.InvalidPlayerNameException;
import org.project.game.model.PlayerIdentity;
import org.project.security.jwt.JwtUtils;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuestServiceTest {
    private JwtUtils jwtUtils;
    private GuestService service;

    @BeforeEach
    void setUp() {
        jwtUtils = mock(JwtUtils.class);
        when(jwtUtils.generateTokenForGuest(any(), anyInt())).thenReturn("guest-token");
        when(jwtUtils.generateGuestJwtCookie("guest-token", 604800000))
                .thenReturn(ResponseCookie.from("guest", "guest-token").build());
        service = new GuestService(jwtUtils);
    }

    @Test
    void createsGuestWithTrimmedNameAndCookie() {
        GuestSessionResponse response = service.createGuest("  Alice  ");
        PlayerIdentity guest = response.guest();

        assertEquals("Alice", guest.getDisplayName());
        assertEquals("GUEST", guest.getId().type().name());
        assertEquals("guest=guest-token", response.cookie());
        assertEquals(guest, service.getGuest(UUID.fromString(guest.getId().value())));
        assertEquals(guest, service.getCurrentGuest(guest.getId().value()));
        verify(jwtUtils).generateTokenForGuest(any(), eq(604800000));
        verify(jwtUtils).generateGuestJwtCookie("guest-token", 604800000);
    }

    @Test
    void rejectsNullBlankAndTooLongNames() {
        assertThrows(InvalidPlayerNameException.class, () -> service.createGuest(null));
        assertThrows(InvalidPlayerNameException.class, () -> service.createGuest("  "));
        assertThrows(InvalidPlayerNameException.class, () -> service.createGuest("a".repeat(21)));
        assertEquals("abcdefghijklmnopqrst",
                service.createGuest("abcdefghijklmnopqrst").guest().getDisplayName());
    }

    @Test
    void updatesGuestNameAfterValidation() {
        PlayerIdentity guest = service.createGuest("Alice").guest();
        UUID id = UUID.fromString(guest.getId().value());

        service.updateName(id, "  Bob ");
        assertEquals("Bob", service.getGuest(id).getDisplayName());
        assertThrows(InvalidPlayerNameException.class, () -> service.updateName(id, ""));
        assertThrows(IllegalArgumentException.class, () -> service.updateName(UUID.randomUUID(), "Bob"));
    }

    @Test
    void distinguishesMalformedAndUnknownGuestIds() {
        assertThrows(IllegalArgumentException.class, () -> service.getCurrentGuest("not-a-uuid"));
        assertThrows(GuestNotFoundException.class,
                () -> service.getCurrentGuest(UUID.randomUUID().toString()));
        assertNull(service.getGuest(UUID.randomUUID()));
    }
}
