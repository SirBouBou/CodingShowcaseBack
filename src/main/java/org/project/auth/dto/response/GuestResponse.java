package org.project.auth.dto.response;

import org.project.game.model.PlayerId;

public record GuestResponse(
        PlayerId id,
        String displayName
) {
}
