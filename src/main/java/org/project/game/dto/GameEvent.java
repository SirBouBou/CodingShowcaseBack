package org.project.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.project.game.model.GameEventType;

@Getter
@AllArgsConstructor
public class GameEvent {
    private GameEventType type;
    private GameDto game;
}
