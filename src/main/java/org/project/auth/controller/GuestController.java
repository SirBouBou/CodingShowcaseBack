package org.project.auth.controller;

import lombok.RequiredArgsConstructor;
import org.project.auth.dto.request.GuestRequest;
import org.project.auth.dto.response.GuestResponse;
import org.project.auth.dto.response.GuestSessionResponse;
import org.project.auth.service.GuestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class GuestController {
    private final GuestService guestService;

    @PostMapping("/session")
    public ResponseEntity<GuestResponse> createSession(@RequestBody GuestRequest guestRequest) {
        GuestSessionResponse result = guestService.createGuest(guestRequest.preferredName());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.cookie())
                .body(new GuestResponse(result.guest().getId(), result.guest().getDisplayName()));
    }
}
