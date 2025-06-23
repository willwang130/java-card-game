package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;
import com.zixun.cardGame.util.CombatCalculator;

import java.util.Map;

public class DamageAndOnKillStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> map = card.getAction();
        int damage = ((Number) map.getOrDefault("damageOnKill", 0)).intValue();
        int gainHP = ((Number) map.getOrDefault("onKillGainHp", 0)).intValue();

        int dealt = CombatCalculator.dealDamageWithCalc(caster, target, damage);
        if (target.getHp() <= 0) {
            caster.addMaxHp(gainHP);
            caster.addHp(gainHP);
        }

        ActorPair names = new ActorPair(caster, target);
        return target.getHp() <= 0 ? CardEffectResult.success(dealt + " 点伤害击杀敌人，永久获得 " + gainHP + " 点最大生命")
                : CardEffectResult.success(names.casterName + "造成了 " + dealt + " 点伤害");
    }
}
