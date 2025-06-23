package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.StatusNames;
import com.zixun.cardGame.util.ActorPair;

import java.util.HashMap;
import java.util.Map;

public class AddBuffStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> buffs = (Map<String, Object>) value;
        Map<String, Object> buffsChinese = new HashMap<>();
        for (Map.Entry<String, Object> entry : buffs.entrySet()) {
            String buffJson = entry.getKey();
            StatusNames buffEnum = StatusNames.getStatusFromJson(buffJson);
            int amount = ((Number) entry.getValue()).intValue();
            caster.getStatusManager().addBuff(buffEnum, amount);

            buffsChinese.put(StatusNames.getChineseFromStatus(buffEnum), amount);
        }

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success( names.casterName + "获得增益: " + buffsChinese);
    }
}
