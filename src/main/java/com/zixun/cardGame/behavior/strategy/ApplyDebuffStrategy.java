package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.StatusNames;
import com.zixun.cardGame.util.ActorPair;

import java.io.ObjectInputFilter;
import java.util.HashMap;
import java.util.Map;

public class ApplyDebuffStrategy implements CardEffectStrategy{
    boolean applyToSelf;

    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        if (card != null) {
            applyToSelf = Boolean.TRUE.equals(card.getAction().get("self"));
        }
        Map<String, Object> debuffs = (Map<String, Object>) value;
        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, Object> entry : debuffs.entrySet()) {
            String debuffJson = entry.getKey();
            StatusNames debuffEnum = StatusNames.getStatusFromJson(debuffJson);
            int amount = ((Number) entry.getValue()).intValue();

            Character realTarget = applyToSelf ? caster
                    : (target != null ? target : engine.getMonster());

            realTarget.getStatusManager().addDebuff(debuffEnum, amount);


            if (!output.isEmpty()) output.append('\n');
            output.append(StatusNames.getChineseFromStatus(debuffEnum)).append(" x").append(amount);
        }

        ActorPair names = new ActorPair(caster, applyToSelf ? caster : (target != null ? target : engine.getMonster()));
        return CardEffectResult.success(names.targetName + "获得负面: " + output);
    }
}
