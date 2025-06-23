package com.zixun.cardGame.event;

import com.zixun.cardGame.type.GameEngineState;

public class GameStateChangedEvent {
    private final GameEngineState newState;

    public GameStateChangedEvent(GameEngineState newState) {
        this.newState = newState;
    }

    public GameEngineState getNewState() {
        return newState;
    }
}
