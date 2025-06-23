package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.factory.CardFactory;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.ActorPair;

import java.util.Map;

public class AddRandomCardStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Map<String, Object> config = (Map<String, Object>) value;
        String type = (String) config.get("type");
        Integer overrideCost  = ((Integer) config.get("costThisTurn"));

        Card randomCard = CardFactory.getRandomCardOfType(type);
        if (overrideCost != null) {
            randomCard.setCostOverrideThisTurn(overrideCost);
        }
        randomCard.setOneTime(true);

        GameEngine.getInstance().getDeckManager().addToHand(randomCard);

        ActorPair names = new ActorPair(caster, target);
        return CardEffectResult.success(names.casterName + "获得一张随机牌: " + randomCard.getName());
    }
}
