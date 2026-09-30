package org.project.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.project.game.model.GameRoom;
import org.project.game.model.PlayerIdentity;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class GameRoomDto {
    private UUID id;
    private long gameId;
    private int maxPlayers;
    private int activePlayers;
    private PlayerIdentity[] players;

    public GameRoomDto(GameRoom room) {
        this(room.getId(), room.getGameId(), room.getMaxPlayers(), room.getActivePlayers(), room.getPlayers());
    }
}
