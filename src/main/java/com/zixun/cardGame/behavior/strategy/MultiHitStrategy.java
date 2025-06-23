package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.behavior.StrategyMap;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;
import com.zixun.cardGame.util.Log;

import java.util.Map;

public class MultiHitStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> multi = (Map<String, Object>) value;
        int times = ((Number) multi.get("times")).intValue();
        CardEffectStrategy strategy = StrategyMap.get("damage");

        for (int i = 1; i < times; i++) {
            CardEffectResult result = strategy.execute(
                    card.extractDamageBase(), card, caster, target, GameEngine.getInstance());
        }

        return CardEffectResult.success("");
    }
}
