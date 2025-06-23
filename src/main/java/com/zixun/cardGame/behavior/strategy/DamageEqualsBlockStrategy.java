package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

public class DamageEqualsBlockStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int block = caster.getBlock();
        int dealt = CombatCalculator.dealDamageWithCalc(caster, target, block);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "造成等同于格挡的伤害: " + dealt);
    }
}
