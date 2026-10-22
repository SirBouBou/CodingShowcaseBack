package org.project.security.services;

import org.junit.jupiter.api.Test;
import org.project.auth.model.ERole;
import org.project.auth.model.Profile;
import org.project.auth.model.Role;
import org.project.auth.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {
    @Test
    void buildsAuthoritiesAndCopiesUserProperties() {
        Profile profile = new Profile();
        Role role = new Role();
        role.setName(ERole.ROLE_USER);
        User user = new User(7L, "alice", "alice@example.com", "encoded", profile, Set.of(role));

        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(7L, details.getId());
        assertEquals("alice", details.getUsername());
        assertEquals("alice@example.com", details.getEmail());
        assertEquals("encoded", details.getPassword());
        assertSame(profile, details.getProfile());
        assertEquals(Set.of(new SimpleGrantedAuthority("ROLE_USER")), Set.copyOf(details.getAuthorities()));
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }

    @Test
    void equalityUsesIdentityAndHashUsesAllFields() {
        UserDetailsImpl first = new UserDetailsImpl(1L, "first", "first@example.com", "one", Set.of(), null);
        UserDetailsImpl sameId = new UserDetailsImpl(1L, "other", "other@example.com", "two", Set.of(), null);
        UserDetailsImpl differentId = new UserDetailsImpl(2L, "first", "first@example.com", "one", Set.of(), null);

        assertEquals(first, first);
        assertEquals(first, sameId);
        assertEquals(first.hashCode(), first.hashCode());
        assertNotEquals(first, differentId);
        assertNotEquals(first, null);
        assertNotEquals(first, "not user details");
    }
}
