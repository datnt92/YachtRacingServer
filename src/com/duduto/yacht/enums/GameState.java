package com.duduto.yacht.enums;

public enum GameState {

    Cancel(101),
    Waiting(0),
    Close(1),
    Started(2),
    EndGame(3);
    private final int state;

    private GameState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
    
}
