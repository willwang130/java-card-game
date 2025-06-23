package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.StatusNames;
import com.zixun.cardGame.util.ActorPair;

import java.io.ObjectInputFilter;
import java.util.Map;

public class GainBuffThisTurnStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> buffs = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : buffs.entrySet()) {
            String buff = entry.getKey();
            StatusNames buffEnum = StatusNames.getStatusFromJson(buff);
            int amount = ((Number) entry.getValue()).intValue();
            caster.getStatusManager().addOneTurnBuff(buffEnum, amount);
        }
        engine.getController().renderHand();

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "临时获得增益状态: " + buffs);
    }
}
