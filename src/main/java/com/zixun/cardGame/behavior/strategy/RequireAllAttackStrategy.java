package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.CombatCalculator;
import com.zixun.cardGame.util.Log;

import java.util.List;
import java.util.Map;

public class RequireAllAttackStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        List<Card> hand = GameEngine.getInstance().getDeckManager().getHand();
        boolean allAttack = hand.stream().allMatch(c -> "攻击".equals(c.getType()));
        Log.write("allAttack = " + allAttack);
        int baseDmg = ((Number) value).intValue();
        int dealt = 0;
        if (allAttack) {
            dealt = CombatCalculator.dealDamageWithCalc(caster, target, baseDmg);
        }

        return allAttack ? CardEffectResult.success("你造成 " + dealt + " 点伤害") : CardEffectResult.failed("手牌中包含非攻击牌，无法使用");
    }
}
