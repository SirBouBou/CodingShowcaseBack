package org.project.game.ticatactoe;

public enum TicTacToeCell {
    EMPTY(0),
    X(1),
    O(-1);

    private final int value;
    private TicTacToeCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
