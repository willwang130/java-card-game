package com.zixun.cardGame.factory;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.util.CardLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardFactory {
    private static final List<Card> getAllCardWithoutBasic =
            new ArrayList<>(CardLoader.getALLCards()
                    .values()
                    .stream().filter(card ->
                                        !(card.getName().equals("打击"))
                                    && !(card.getName().equals("防御"))
                                    && !(card.getName().equals("痛击"))
                    )
                    .toList()
            );

    public static List<Card> getRandomCards(int count) {
        List<Card> copy = new ArrayList<>(getAllCardWithoutBasic);
        Collections.shuffle(copy);
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < count && i <copy.size(); i++) {
            result.add(new Card(copy.get(i)));
        }
        return result;
    }

    public static Card getCardByName (String cardName) {
        Card card = CardLoader.getALLCards().get(cardName);
        if (card == null) throw new RuntimeException("未知卡牌名: " + cardName);
        return new Card(card);
    }

    public static Card getRandomCardOfType(String type) {
        List<Card> copy = new ArrayList<>(getAllCardWithoutBasic);
        List<Card> ofType = new ArrayList<>(copy.stream().filter(c -> type.equals(c.getType())).toList());
        Collections.shuffle(ofType);
        return new Card(ofType.get(0));
    }
}
