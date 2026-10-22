package org.project.security.services;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuestUserDetailsImplTest {
    @Test
    void exposesGuestIdentityAndEnabledAccountFlags() {
        UUID id = UUID.randomUUID();
        GuestUserDetailsImpl details = new GuestUserDetailsImpl(id);

        assertEquals(id, details.getGuestId());
        assertEquals(id.toString(), details.getUsername());
        assertNull(details.getPassword());
        assertEquals(java.util.List.of(new SimpleGrantedAuthority("ROLE_GUEST")), details.getAuthorities());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
