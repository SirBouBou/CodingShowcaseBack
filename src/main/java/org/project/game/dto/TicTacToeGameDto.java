package org.project.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.project.game.model.GameStatus;
import org.project.game.model.PlayerIdentity;
import org.project.game.ticatactoe.TicTacToeCell;
import org.project.game.ticatactoe.TicTacToeGame;

@Getter
@AllArgsConstructor
public class TicTacToeGameDto implements GameDto {
    private PlayerIdentity playerX;
    private PlayerIdentity playerO;
    private TicTacToeCell[] board;
    private PlayerIdentity currentPlayer;
    private PlayerIdentity winner;
    private GameStatus status;

    public TicTacToeGameDto(TicTacToeGame game) {
        this(
                game.getPlayerX(),
                game.getPlayerO(),
                game.getBoard(),
                game.getCurrentPlayer(),
                game.getWinner(),
                game.getStatus()
        );
    }
}
