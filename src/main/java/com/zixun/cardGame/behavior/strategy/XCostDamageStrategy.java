package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.CombatCalculator;

import java.util.Map;

public class XCostDamageStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> map = card.getAction();
        int per = ((Number) map.get("damagePerEnergy")).intValue();

        int energy = caster.getEp();
        int total = 0;
        for (int i = 0; i < energy; i++) {
            total += CombatCalculator.dealDamageWithCalc(caster, target, per);
        }
        caster.minusEp(energy);
        return CardEffectResult.success("你消耗 " + energy + " 点能量，总计造成 " + total + " 点伤害");
    }
}
