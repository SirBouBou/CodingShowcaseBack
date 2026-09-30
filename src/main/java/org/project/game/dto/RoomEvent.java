package org.project.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.project.game.model.RoomEventType;

@Getter
@AllArgsConstructor
public class RoomEvent {
    private RoomEventType type;
    private GameRoomDto room;
}
