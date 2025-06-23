package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.TriggerTypes;
import com.zixun.cardGame.util.ActorPair;

public class DoubleBlockStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int doubled = caster.getBlock();
        caster.addBlock(doubled);

        GameEngine.getInstance().getTriggerManager()
                .trigger(TriggerTypes.ON_GAIN_BLOCK, caster, () -> {});

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "将当前格挡翻倍，获得了额外 " + doubled + " 点格挡");
    }
}
