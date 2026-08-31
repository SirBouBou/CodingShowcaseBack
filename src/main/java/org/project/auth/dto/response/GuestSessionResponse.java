package org.project.auth.dto.response;

import lombok.AllArgsConstructor;
import org.project.game.model.PlayerIdentity;

public record GuestSessionResponse (
        PlayerIdentity guest,
        String cookie
)
{}
