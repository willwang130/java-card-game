package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;

public class CopyCardStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        int count = ((Number) value).intValue();
        for (int i = 0; i < count; i++) {
            GameEngine.getInstance().getDeckManager().addCopyToDiscard(card);
        }
        return CardEffectResult.success("弃牌堆里添加了 " + count + " 张当前卡牌的副本");
    }
}
