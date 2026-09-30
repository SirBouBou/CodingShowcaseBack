package org.project.game.ticatactoe;

import lombok.Getter;
import lombok.Setter;
import org.project.game.dto.GameDto;
import org.project.game.dto.TicTacToeGameDto;
import org.project.game.model.GameMove;
import org.project.game.model.GamePlay;
import org.project.game.model.GameStatus;
import org.project.game.model.PlayerIdentity;

import java.util.Arrays;

@Getter
@Setter
public class TicTacToeGame implements GamePlay {

    private PlayerIdentity playerX;
    private PlayerIdentity playerO;

    private TicTacToeCell[] board;

    private PlayerIdentity currentPlayer;
    private PlayerIdentity winner;

    private GameStatus status;

    public TicTacToeGame(PlayerIdentity creator) {
        this.playerX = creator;
        this.playerO = null;
        this.board = new TicTacToeCell[9];
        Arrays.fill(this.board, TicTacToeCell.EMPTY);
        this.status = GameStatus.WAITING;
    }

    public void reset() {
        this.board = new TicTacToeCell[9];
        Arrays.fill(this.board, TicTacToeCell.EMPTY);
        this.currentPlayer = null;
        this.winner = null;
        this.status = GameStatus.WAITING;
    }

    public boolean addPlayer(PlayerIdentity player) {
        if(this.status == GameStatus.WAITING) {
            if(playerO == null) {
                this.playerO = player;
            } else if (playerX == null) {
                this.playerX = player;
            } else {
                return false;
            }

            int random = (int)(Math.random()*2);
            if(random == 0) {
                currentPlayer = this.playerX;
            } else {
                currentPlayer = this.playerO;
            }
            this.status = GameStatus.PLAYING;
            return true;
        }
        return false;
    }

    public boolean removePlayer(PlayerIdentity player) {
        if(player.equals(this.playerX)) {
            this.playerX = null;
        } else if(player.equals(this.playerO)) {
            this.playerO = null;
        } else {
            return false;
        }
        this.reset();
        return true;
    }

    public boolean play(GameMove move) {
        if(status != GameStatus.PLAYING) {
            return false;
        }
        PlayerIdentity player = move.player();

        if (!"PLAY".equals(move.action())) {
            return false;
        }

        int position = (int) move.data();

        if(position < 0 || position > 8) {
            return false;
        }

        TicTacToeCell temp = TicTacToeCell.EMPTY;
        if(player.getId().equals(playerX.getId())) {
            temp = TicTacToeCell.X;
        } else if (player.getId().equals(playerO.getId())) {
            temp = TicTacToeCell.O;
        } else {
            return false;
        }

        if (!player.getId().equals(currentPlayer.getId())) {
            return false;
        }

        if (board[position] != TicTacToeCell.EMPTY) {
            return false;
        }

        board[position] = temp;
        if(!checkEnd()) {
            nextTurn();
        }
        return true;
    }

    public void nextTurn() {
        if(currentPlayer.getId().equals(playerX.getId())) {
            currentPlayer = playerO;
        } else {
            currentPlayer = playerX;
        }
    }

    public Boolean checkEnd() {
        int caseToEnd = 9;
        for (int i = 0; i < 9; i++) {
            if(board[i] != TicTacToeCell.EMPTY) {
                caseToEnd--;
            }
        }
        int[] result = {0,0,0,0,0,0,0,0};//r1,r2,r3,c1,c2,c3,d1,d2
        for(int i = 0; i < 3; i++) {
            result[0] += board[i].getValue();
            result[1] += board[i+3].getValue();
            result[2] += board[i+6].getValue();
            result[3] += board[i*3].getValue();
            result[4] += board[i*3+1].getValue();
            result[5] += board[i*3+2].getValue();
            result[6] += board[i*4].getValue();
            result[7] += board[(i+1)*2].getValue();
        }

        for(int val : result) {
            if(val == 3) {
                status = GameStatus.FINISHED;
                winner = playerX;
                return true;
            } else if(val == -3) {
                status = GameStatus.FINISHED;
                winner = playerO;
                return true;
            }
        }

        if (caseToEnd == 0) {
            status = GameStatus.DRAW;
            winner = null;
            return true;
        }
        return false;
    }

    @Override
    public GameDto getDto() {
        return new TicTacToeGameDto((this));
    }
}
