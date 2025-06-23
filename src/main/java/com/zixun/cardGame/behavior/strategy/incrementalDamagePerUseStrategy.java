package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

import java.util.Map;

public class incrementalDamagePerUseStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> map = card.getAction();
        int incrementalDamagePerUse = ((Number) map.get("incrementalDamagePerUse")).intValue();
        int baseDamage = ((Number) map.get("baseDamage")).intValue();
        int total = baseDamage + (incrementalDamagePerUse * card.getTimesUsedThisBattle());
        CombatCalculator.dealDamageWithCalc(caster, target, total);
        card.increaseTimeUsedThisBattle();

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "对 " + names.targetName + " 造成了 " + total + " 点伤害");
    }
}
