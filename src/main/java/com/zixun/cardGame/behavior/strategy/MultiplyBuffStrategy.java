package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.StatusNames;
import com.zixun.cardGame.util.ActorPair;

import java.util.Map;

public class MultiplyBuffStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> buffs = (Map<String, Object>) value;
        StatusNames name = null;
        for (String buffJson : buffs.keySet()) {
            name = StatusNames.getStatusFromJson(buffJson);
            int base = caster.getStatusManager().get(name);
            caster.getStatusManager().addOneBattleBuff(name, base);
        }

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "将指定 Buff 状态翻倍: " + StatusNames.getChineseFromStatus(name));
    }
}
