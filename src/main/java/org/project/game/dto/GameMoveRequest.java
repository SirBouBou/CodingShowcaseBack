package org.project.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMoveRequest {
    private String action;
    private Object data;
}
