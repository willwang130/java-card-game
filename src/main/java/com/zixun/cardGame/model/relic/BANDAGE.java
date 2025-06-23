package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.type.StatusNames;

import java.util.Map;

import static com.zixun.cardGame.type.StatusNames.BANDAGE;
import static com.zixun.cardGame.type.TriggerTypes.ON_KILL;

public class BANDAGE extends Relic {
    private static final StatusNames sourceId = BANDAGE; //"绷带"
    private static final String description = "每击败一个敌人, 玩家恢复 3 点生命值";
    private static final int price = 80;

    public BANDAGE() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        engine.getTriggerManager().registerOrMergeTrigger(
                sourceId.name(),
                SourceType.RELIC,
                ON_KILL,
                Map.of("gainHp", 3)
        );
    }
}
