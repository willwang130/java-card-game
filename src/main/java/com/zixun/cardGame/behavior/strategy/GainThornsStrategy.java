package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;

public class GainThornsStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int amount = ((Number) value).intValue();
        caster.getStatusManager().addPermanentThorns(amount);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "永久获得 " + amount + " 点反伤");
    }
}
