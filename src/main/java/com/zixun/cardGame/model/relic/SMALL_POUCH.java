package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.type.StatusNames;

import java.util.Map;

import static com.zixun.cardGame.type.StatusNames.SMALL_POUCH;
import static com.zixun.cardGame.type.TriggerTypes.BATTLE_START;

public class SMALL_POUCH extends Relic {
    private static final StatusNames sourceId = SMALL_POUCH; //"小口袋";
    private static final String description = "第一回合抽牌数 + 2";
    private static final int price = 90;

    public SMALL_POUCH() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        engine.getTriggerManager().registerOrMergeTrigger(
                sourceId.name(),
                SourceType.RELIC,
                BATTLE_START,
                Map.of("draw", 2)
        );
    }
}
