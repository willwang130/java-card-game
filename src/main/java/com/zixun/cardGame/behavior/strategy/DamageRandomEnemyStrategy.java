package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

public class DamageRandomEnemyStrategy implements CardEffectStrategy{

    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int baseDmg = ((Number) value).intValue();
        int dealt = CombatCalculator.dealDamageWithCalc(caster, target, baseDmg);

        ActorPair names = new ActorPair(caster, target);
        return  CardEffectResult.success("获得护甲时" + names.casterName + "对 " + names.casterName + " 造成 " + dealt + " 点伤害");
    }
}
