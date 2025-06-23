package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.type.StatusNames;

import java.util.Map;

import static com.zixun.cardGame.type.StatusNames.GUARD_EMBLEM;
import static com.zixun.cardGame.type.TriggerTypes.TURN_START;

public class GUARD_EMBLEM extends Relic {
    private static final StatusNames sourceId = GUARD_EMBLEM; // 守护徽章
    private static final String description = "每回合开始时获得 5 点护甲";
    private static final int price = 120;

    public GUARD_EMBLEM() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        engine.getTriggerManager().registerOrMergeTrigger(sourceId.name(), SourceType.RELIC, TURN_START,
                Map.of("gainBlock", 5)
        );
    }
}
