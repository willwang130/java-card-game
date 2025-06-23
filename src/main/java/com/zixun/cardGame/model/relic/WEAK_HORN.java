package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.type.StatusNames;

import java.util.Map;

import static com.zixun.cardGame.type.StatusNames.WEAK_HORN;
import static com.zixun.cardGame.type.TriggerTypes.BATTLE_START;

public class WEAK_HORN extends Relic {
    private static final StatusNames sourceId =  WEAK_HORN; //"虚弱号角";
    private static final String description = "战斗开始时使所有敌人获得 2 层虚弱";
    private static final int price = 100;

    public WEAK_HORN() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        engine.getTriggerManager().registerOrMergeTrigger(
                sourceId.name(),
                SourceType.RELIC,
                BATTLE_START,
                Map.of("applyDebuff", Map.of("weak", 2)
        ));
    }
}
