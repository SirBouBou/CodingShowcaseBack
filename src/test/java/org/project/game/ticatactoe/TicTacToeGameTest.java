package org.project.game.ticatactoe;

import org.junit.jupiter.api.Test;
import org.project.game.dto.TicTacToeGameDto;
import org.project.game.model.GameMove;
import org.project.game.model.GameStatus;
import org.project.game.model.PlayerId;
import org.project.game.model.PlayerIdentity;
import org.project.game.model.PlayerType;

import static org.junit.jupiter.api.Assertions.*;

class TicTacToeGameTest {
    private final PlayerIdentity playerX = player("x");
    private final PlayerIdentity playerO = player("o");

    @Test
    void initializesAndResetsBoard() {
        TicTacToeGame game = new TicTacToeGame(playerX);

        assertEquals(playerX, game.getPlayerX());
        assertNull(game.getPlayerO());
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertArrayEquals(new TicTacToeCell[]{
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY,
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY,
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY
        }, game.getBoard());

        game.addPlayer(playerO);
        game.setCurrentPlayer(playerX);
        game.setWinner(playerO);
        game.getBoard()[0] = TicTacToeCell.X;

        game.reset();

        assertEquals(GameStatus.WAITING, game.getStatus());
        assertNull(game.getCurrentPlayer());
        assertNull(game.getWinner());
        assertArrayEquals(new TicTacToeCell[]{
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY,
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY,
                TicTacToeCell.EMPTY, TicTacToeCell.EMPTY, TicTacToeCell.EMPTY
        }, game.getBoard());
    }

    @Test
    void startsWhenSecondPlayerJoinsAndRejectsFurtherPlayers() {
        TicTacToeGame game = new TicTacToeGame(playerX);

        assertTrue(game.addPlayer(playerO));
        assertEquals(GameStatus.PLAYING, game.getStatus());
        assertTrue(game.getCurrentPlayer().equals(playerX) || game.getCurrentPlayer().equals(playerO));
        assertFalse(game.addPlayer(player("third")));
    }

    @Test
    void removesKnownPlayerAndResetsButRejectsUnknownPlayer() {
        TicTacToeGame game = new TicTacToeGame(playerX);
        game.addPlayer(playerO);
        game.setCurrentPlayer(playerX);
        game.play(move(playerX, 0));

        assertFalse(game.removePlayer(player("unknown")));
        assertTrue(game.removePlayer(playerO));
        assertNull(game.getPlayerO());
        assertNull(game.getWinner());
        assertNull(game.getCurrentPlayer());
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(TicTacToeCell.EMPTY, game.getBoard()[0]);
    }

    @Test
    void rejectsMovesWhenGameIsNotPlayingOrMoveIsInvalid() {
        TicTacToeGame game = new TicTacToeGame(playerX);
        assertFalse(game.play(move(playerX, 0)));
        game.addPlayer(playerO);

        assertFalse(game.play(new GameMove(playerX, "PASS", 0)));
        assertFalse(game.play(move(playerX, -1)));
        assertFalse(game.play(move(playerX, 9)));
        assertFalse(game.play(move(player("unknown"), 0)));

        PlayerIdentity otherTurn = game.getCurrentPlayer().equals(playerX) ? playerO : playerX;
        assertFalse(game.play(move(otherTurn, 0)));
        assertTrue(game.getBoard()[0] == TicTacToeCell.EMPTY);
    }

    @Test
    void acceptsOnlyAnEmptyCellAndAdvancesTurn() {
        TicTacToeGame game = new TicTacToeGame(playerX);
        game.addPlayer(playerO);
        game.setCurrentPlayer(playerX);

        assertTrue(game.play(move(playerX, 4)));
        assertEquals(TicTacToeCell.X, game.getBoard()[4]);
        assertEquals(playerO, game.getCurrentPlayer());
        assertFalse(game.play(move(playerO, 4)));
        assertEquals(GameStatus.PLAYING, game.getStatus());
    }

    @Test
    void detectsWinsForBothPlayers() {
        TicTacToeGame xGame = playingGame();
        PlayerIdentity starter = xGame.getCurrentPlayer();
        PlayerIdentity opponent = otherPlayer(starter);
        play(xGame, starter, 0);
        play(xGame, opponent, 3);
        play(xGame, starter, 1);
        play(xGame, opponent, 4);
        play(xGame, starter, 2);
        assertEquals(GameStatus.FINISHED, xGame.getStatus());
        assertEquals(starter, xGame.getWinner());
        assertTrue(xGame.checkEnd());

        TicTacToeGame oGame = playingGame();
        starter = oGame.getCurrentPlayer();
        opponent = otherPlayer(starter);
        play(oGame, starter, 1);
        play(oGame, opponent, 0);
        play(oGame, starter, 2);
        play(oGame, opponent, 3);
        play(oGame, starter, 4);
        play(oGame, opponent, 6);
        assertEquals(GameStatus.FINISHED, oGame.getStatus());
        assertEquals(opponent, oGame.getWinner());
    }

    @Test
    void detectsDrawWithoutWinner() {
        TicTacToeGame game = playingGame();
        int[] positions = {0, 1, 2, 4, 3, 5, 7, 6, 8};
        for (int i = 0; i < positions.length; i++) {
            play(game, game.getCurrentPlayer(), positions[i]);
        }

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertNull(game.getWinner());
        assertTrue(game.checkEnd());
    }

    @Test
    void exposesGameDto() {
        TicTacToeGame game = new TicTacToeGame(playerX);
        TicTacToeGameDto dto = assertInstanceOf(TicTacToeGameDto.class, game.getDto());

        assertEquals(playerX, dto.getPlayerX());
        assertNull(dto.getPlayerO());
        assertEquals(GameStatus.WAITING, dto.getStatus());
        assertEquals(9, dto.getBoard().length);
    }

    private TicTacToeGame playingGame() {
        TicTacToeGame game = new TicTacToeGame(playerX);
        assertTrue(game.addPlayer(playerO));
        return game;
    }

    private void play(TicTacToeGame game, PlayerIdentity player, int position) {
        assertEquals(player, game.getCurrentPlayer());
        assertTrue(game.play(move(player, position)));
        if (game.getStatus() == GameStatus.PLAYING) {
            assertEquals(otherPlayer(player), game.getCurrentPlayer());
        }
    }

    private PlayerIdentity otherPlayer(PlayerIdentity player) {
        return player.equals(playerX) ? playerO : playerX;
    }

    private static GameMove move(PlayerIdentity player, int position) {
        return new GameMove(player, "PLAY", position);
    }

    private static PlayerIdentity player(String id) {
        return new PlayerIdentity(new PlayerId(PlayerType.USER, id), id);
    }
}
