package com.zixun.cardGame.behavior;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;

public class EpCheck implements CardHandlerChain{

   private final CardHandlerChain next;

   public EpCheck(CardHandlerChain next) {
       this.next = next;
   }

    @Override
    public CardEffectResult apply(Card card, Character caster, Character target) {
       // TODO: MP 保留？
        if (caster.getEp() < card.getCost()) {
            return CardEffectResult.failed("EP 不足!");
        }else {
            caster.minusEp(card.getCost());
        }
        return next.apply(card, caster, target);
    }
}
