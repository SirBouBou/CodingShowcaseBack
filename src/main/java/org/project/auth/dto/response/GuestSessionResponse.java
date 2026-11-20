package org.project.auth.dto.response;

import org.project.game.model.PlayerIdentity;

public record GuestSessionResponse(
        PlayerIdentity guest,
        String cookie
) {
}
