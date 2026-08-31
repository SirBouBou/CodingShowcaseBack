package org.project.auth.service;

import lombok.RequiredArgsConstructor;
import org.project.auth.dto.response.GuestSessionResponse;
import org.project.auth.exception.InvalidPlayerNameException;
import org.project.game.model.PlayerId;
import org.project.game.model.PlayerIdentity;
import org.project.game.model.PlayerType;
import org.project.security.jwt.JwtUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GuestService {

    private static final int MAX_NAME_LENGTH = 20;
    private static final int GUEST_EXPIRATION_MS = 604800000;
    private final JwtUtils jwtUtils;

    private final Map<UUID, PlayerIdentity> guests = new ConcurrentHashMap<>();

    public GuestSessionResponse createGuest(String preferredName) {
        String name = validateAndNormalizeName(preferredName);
        UUID id = UUID.randomUUID();
        PlayerIdentity guest = new PlayerIdentity(
                new PlayerId(
                        PlayerType.GUEST,
                        id.toString()
                ),
                name
        );

        guests.put(id, guest);

        String token = jwtUtils.generateTokenForGuest(id, GUEST_EXPIRATION_MS);
        ResponseCookie cookie = jwtUtils.generateGuestJwtCookie(token, GUEST_EXPIRATION_MS);
        return new GuestSessionResponse(guest, cookie.toString());
    }

    public PlayerIdentity getGuest(UUID id) {
        return guests.get(id);
    }

    public void updateName(UUID id, String name) {
        PlayerIdentity guest = guests.get(id);

        if (guest == null) {
            throw new IllegalArgumentException("Guest not found");
        }

        guest.setDisplayName(validateAndNormalizeName(name));
    }

    private String validateAndNormalizeName(String name) {
        if (name == null) {
            throw new InvalidPlayerNameException(
                    "Le pseudo ne peut pas être vide."
            );
        }

        String normalized = name.trim();

        if (normalized.isEmpty()) {
            throw new InvalidPlayerNameException("Le pseudo ne peut pas être vide");
        }

        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new InvalidPlayerNameException(
                    "Le pseudo ne peut pas dépasser " + MAX_NAME_LENGTH + " caractères."
            );
        }

        return normalized;
    }
}
