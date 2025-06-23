package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

import java.util.Map;

public class LifeStealStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {

        int amount;

        if (value instanceof Number n) {                     // 怪物 AI 分支
            amount = n.intValue();

        } else if (value instanceof Map<?,?> m               // 万一以后写成 Map
                && m.get("lifeSteal") instanceof Number n) {
            amount = n.intValue();

        } else if (card != null                              // 卡牌分支
                && card.getAction().get("lifeSteal") instanceof Number n) {
            amount = n.intValue();

        } else {
            return CardEffectResult.failed("lifeSteal 参数错误");
        }

        int dealt = CombatCalculator.dealDamageWithCalc(caster, target, amount);
        caster.addHp(dealt);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "对" + names.targetName +"造成 " + dealt + " 点伤害并获得" + dealt + " hp");
    }
}
