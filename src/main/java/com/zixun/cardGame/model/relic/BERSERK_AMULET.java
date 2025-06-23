package com.zixun.cardGame.model.relic;

import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.StatusNames;

import static com.zixun.cardGame.type.StatusNames.BERSERK_AMULET;

public class BERSERK_AMULET extends Relic {
    private static final StatusNames sourceId = BERSERK_AMULET;
    private static final String description = "力量 + 2";
    private static final int price = 120;

    public BERSERK_AMULET() {
        super(StatusNames.getChineseFromStatus(sourceId), description, price);
    }

    @Override
    public void onAcquired(Player player, GameEngine engine) {
        player.getStatusManager().addBuff(StatusNames.STRENGTH, 2);
    }
}
