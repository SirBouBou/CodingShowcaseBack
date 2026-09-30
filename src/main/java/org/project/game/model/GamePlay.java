package org.project.game.model;


import org.project.game.dto.GameDto;

public interface GamePlay {
    GameStatus getStatus();
    PlayerIdentity getWinner();
    boolean addPlayer(PlayerIdentity player);
    boolean removePlayer(PlayerIdentity player);
    boolean play(GameMove move);
    GameDto getDto();
}
