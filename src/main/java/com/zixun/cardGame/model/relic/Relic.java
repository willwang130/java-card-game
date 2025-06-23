package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.manager.TriggerManager;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.StatusNames;

import java.util.Objects;

public abstract class Relic {
    private final String NAME;
    private final String DESCRIPTION;
    private final int PRICE;

    public Relic(String name, String description, int price) {
        this.NAME = name;
        this.DESCRIPTION = description;
        this.PRICE = price;
    }

    public abstract void onAcquired(Player player, GameEngine engine);

    public String getName() { return NAME; }
    public String getDescription() { return DESCRIPTION; }
    public int getPrice() { return PRICE; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relic relic = (Relic) o;
        return Objects.equals(NAME, relic.NAME); // 假设 name 唯一标识一个 relic
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}

