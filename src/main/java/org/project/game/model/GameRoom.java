package org.project.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.project.game.dto.GameDto;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class GameRoom {
    private UUID id;
    private long gameId;
    private int maxPlayers;
    private int activePlayers;
    private PlayerIdentity[] players;
    private GamePlay game;

    public boolean addPlayer(PlayerIdentity player) {
        if(this.activePlayers == this.maxPlayers) {
            return false;
        }
        for(int i = 0; i < maxPlayers; i++) {
            if(player.equals(this.players[i])) {
                return false;
            }
        }

        if(!game.addPlayer(player)) {
            return false;
        }
        for(int i = 0; i < maxPlayers; i++) {
            if(this.players[i] == null) {
                this.players[i] = player;
                this.activePlayers++;
                return true;
            }
        }
        return false;
    }

    public boolean removePlayer(PlayerIdentity player) {
        for(int i = 0; i < maxPlayers; i++) {
            if(player.equals(this.players[i])) {
                if(!game.removePlayer(player)) {
                    return false;
                }
                this.players[i] = null;
                this.activePlayers--;
                return true;
            }
        }
        return false;
    }

    public boolean play(PlayerIdentity player, GameMove move) {
        for (int i = 0; i < maxPlayers; i++) {
            if (player.equals(this.players[i])) {
                return game.play(move);
            }
        }
        return false;
    }

    public GameDto getGameDto() {
        return game.getDto();
    }
}
