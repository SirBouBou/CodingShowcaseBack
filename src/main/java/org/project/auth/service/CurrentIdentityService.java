package org.project.auth.service;

import lombok.RequiredArgsConstructor;
import org.project.game.model.PlayerIdentity;
import org.project.security.services.GuestUserDetailsImpl;
import org.project.security.services.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentIdentityService {

    private final AuthService authService;
    private final GuestService guestService;

    public PlayerIdentity getCurrentIdentity(Authentication authentication) {
        if (authentication.getPrincipal() instanceof GuestUserDetailsImpl guest) {
            return guestService.getCurrentGuest(guest.getGuestId().toString());
        }

        if (authentication.getPrincipal() instanceof UserDetailsImpl guest) {
            return authService.getCurrentUser(guest.getId());
        }
        throw new AccessDeniedException("Unknown player identity");
    }
}
