package org.project.game.model;

public record GameMove(
        PlayerIdentity player,
        String action,
        Object data
) {
}
