package com.zixun.cardGame.manager;
import com.zixun.cardGame.factory.CardFactory;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;

import java.util.*;

public class DeckManager {
    private final LinkedList<Card> deck = new LinkedList<>();
    private final LinkedList<Card> hand = new LinkedList<>();
    private final LinkedList<Card> discard = new LinkedList<>();
    private final LinkedList<Card> exhaust = new LinkedList<>();
    private final LinkedList<Card> power = new LinkedList<>();

    private final Map<String, List<String>> initialDeckMap = Map.of(
            "warrior", List.of("壁垒", "巩固", "火焰屏障", "打击", "打击",
                    "金属化", "打击", "打击", "缴械", "势不可挡", "屹然不动")
    );

    public static final int MAX_DECK = 15;
    public static int HAND_SIZE = 8;
    public static int DRAW_MIN = 5;

    private final List<DrawListener> listeners = new ArrayList<>();
    // 注册 / 注销
    public void addDrawListener(DrawListener l)   { listeners.add(l);  }
    public void removeDrawListener(DrawListener l){ listeners.remove(l);}

    public void initDeckForClass(String playerClass) {
        deck.clear(); hand.clear(); discard.clear(); exhaust.clear();

        List<String> cardNames = initialDeckMap.get(playerClass);

        for (String name : cardNames) {
            Card card = CardFactory.getCardByName(name);
            deck.add(card);
        }

        Collections.shuffle(deck);
    }

    public int draw(int drawCount) {
        int count = 0;
        while (hand.size() < HAND_SIZE && count < drawCount) {
            if (deck.isEmpty()) reShuffle();
            if (!deck.isEmpty()) {
                Card card = deck.poll();
                hand.add(card);
                listeners.forEach(l -> l.onCardDrawn(card));
                count++;
            }
        }
        return count;
    }

    public Card drawOneButPeek() {
        Card temp = new Card();
        if (hand.size() < HAND_SIZE) {
            if (deck.isEmpty()) reShuffle();
            if (!deck.isEmpty()) {
                Card card = deck.poll();
                temp = card;
                //hand.add(temp);
                //listeners.forEach(l -> l.onCardDrawn(card));
            }
        }
        return temp;
    }

    public int drawToFull() {
        int count = 0;
        while (hand.size() < HAND_SIZE) {
            if (deck.isEmpty()) reShuffle();
            if (!deck.isEmpty()) {
                hand.add(deck.poll());
                count++;
            }
        }
        return count;
    }
    public void tryToDiscard(Card card) {
        hand.remove(card);
        if ("能力".equals(card.getType())) {
            power.add(card);
        } else if (card.getExhaustAfterUse()) {
            exhaust.add(card);
        } else {
            discard.add(card);
        }
    }

    public void discardCardInHand(Card card) {
        hand.remove(card);
        discard.add(card);
    }
    public void discardAllCardInHand() {
        List<Card> handCopy = new ArrayList<>(hand);
        for (Card card : handCopy) {
            discardCardInHand(card);
        }
    }

    public void startBattleSetUp() {
        startBattleShuffle();
        getAllCards().forEach(Card::resetTimeUsedThisBattle);
    }


    public void endBattleSetUp() {
        getAllCards().forEach(card -> {
            if (card.getOneTime()) {
                removeCard(card);
            }
        });
        discardAllCardInHand();
    }

    public void reShuffle() {
        deck.addAll(discard);
        discard.clear();
        Collections.shuffle(deck);
    }
    public void startBattleShuffle() {
        deck.addAll(hand);
        hand.clear();
        deck.addAll(discard);
        discard.clear();
        deck.addAll(exhaust);
        exhaust.clear();
        deck.addAll(power);
        power.clear();
        Collections.shuffle(deck);
    }

    public List<Card> getAllCards() {
        List<Card> all = new ArrayList<>();
        all.addAll(deck);
        all.addAll(hand);
        all.addAll(discard);
        all.addAll(exhaust);
        all.addAll(power);
        return all;
    }
    public boolean addCard(Card card) {
        return deck.add(card);
    }
    public boolean removeCard(Card card) {
        if (deck.remove(card) || hand.remove(card) ||
                discard.remove(card) || exhaust.remove(card) || power.remove(card)) {
            return true;
        } else {
            System.out.println("尝试移除卡牌失败：" + card.getName());
            return false;
        }
    }
    public void addCopyToDiscard(Card card) {
        discard.add(new Card(card));
    }

    public void exhaustCard(Card card) {
        exhaust.add(new Card(card));
        removeCard(card);
    }

    public void addToHand(Card random) {
        hand.add(new Card(random));
    }

    public List<Card> getHand() {
        return hand;
    }
    public List<Card> getDeckPile() { return deck; }
    public List<Card> getDiscardPile() { return discard; }
    public List<Card> getExhaustPile() { return exhaust; }
    public int getDeckSize() { return getDeckPile().size(); }
    public List<Card> getDiscard() { return discard; }
    public int getDiscardSize() { return getDiscard().size(); }

    public int countUpgradable() {
        return (int) getAllCards().stream().filter(Card::canUpgrade).count();
    }

    public Card getCardById(String cardId) {
        return hand.stream()
                .filter(card -> card.getInstanceId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    public void clearCostOverrideThisTurn() {
        getAllCards().forEach(Card::setCostOverrideThisTurnToNull);
    }

    public interface DrawListener {
        void onCardDrawn(Card card);      // 每抽到一张牌立即回调
    }

    public void reset() {
        deck.clear(); hand.clear(); discard.clear(); exhaust.clear();
    }
}
