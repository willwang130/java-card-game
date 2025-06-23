package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.behavior.StrategyMap;
import com.zixun.cardGame.behavior.strategy.CardEffectStrategy;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.StatusNames;

import static com.zixun.cardGame.type.StatusNames.THORNS_AMULET;

public class THORNS_AMULET extends Relic {
    private static final StatusNames sourceId = THORNS_AMULET; //"荆棘护符";
    private static final String description = "获得 5 点永久反伤";
    private static final int price = 130;

    public THORNS_AMULET() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        CardEffectStrategy strategy = StrategyMap.get("addThorns");
        if (strategy != null) {
            strategy.execute(5, null, player, null, engine);
        }
    }
}
