package com.zixun.cardGame.behavior;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;

public interface CardHandlerChain {
    CardEffectResult apply(Card card, Character caster, Character target);
}
