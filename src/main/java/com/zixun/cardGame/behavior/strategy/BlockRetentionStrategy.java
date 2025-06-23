package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;

public class BlockRetentionStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        caster.setBlockRetention(true);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success( names.casterName + "本场战斗内格挡将不再回合开始时消失");
    }
}
