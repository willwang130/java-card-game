package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.TriggerTypes;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

import static com.zixun.cardGame.type.StatusNames.DEXTERITY;
import static com.zixun.cardGame.type.StatusNames.FRAIL;

public class GainBlockStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int block = ((Number) value).intValue();
        int mod = CombatCalculator.calcBlock(block, caster);
        caster.addBlock(mod);

        GameEngine.getInstance().getTriggerManager()
                .trigger(TriggerTypes.ON_GAIN_BLOCK, caster, () -> {});

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "获得了 " + block + " 点格挡");
    }
}
