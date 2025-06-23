package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;

public class LoseHpStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int loss = ((Number) value).intValue();
        caster.addHp(-loss);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "失去了 " + loss + " 点生命");
    }
}
