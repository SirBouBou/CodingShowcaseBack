package org.project.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class GuestSession {
    private final String sessionId;
    private final String guestId;
    private Instant expiresAt;
}
