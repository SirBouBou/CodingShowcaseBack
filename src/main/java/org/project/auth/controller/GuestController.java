package org.project.auth.controller;

import lombok.RequiredArgsConstructor;
import org.project.auth.dto.request.GuestRequest;
import org.project.auth.dto.response.GuestSessionResponse;
import org.project.auth.service.GuestService;
import org.project.game.model.PlayerIdentity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class GuestController {
    private final GuestService guestService;

    @PostMapping("/session")
    public ResponseEntity<PlayerIdentity> createSession(@RequestBody GuestRequest guestRequest) {
        GuestSessionResponse result = guestService.createGuest(guestRequest.preferredName());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie())
                .body(new PlayerIdentity(result.guest().getId(), result.guest().getDisplayName()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<PlayerIdentity> me(Authentication authentication) {
        return ResponseEntity.ok(guestService.getCurrentGuest(authentication.getName()));
    }
}
