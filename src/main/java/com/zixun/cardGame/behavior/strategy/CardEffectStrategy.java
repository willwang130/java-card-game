package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;

public interface CardEffectStrategy {
    CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine);
}
