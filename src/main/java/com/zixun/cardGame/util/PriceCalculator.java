package com.zixun.cardGame.util;

import com.zixun.cardGame.model.card.Card;

import java.util.Map;
import java.util.Random;

public class PriceCalculator {
    private static final Map<String, int[]> CARD_PRICE_RANGES = Map.of(
            "common", new int[]{50, 70},
            "uncommon", new int[]{80, 110},
            "rare", new int[]{130, 160}
    );

    public static int getRandomPriceForCard(Card card, boolean isDiscounted, boolean isHalfPrice) {
        int[] range = CARD_PRICE_RANGES.getOrDefault(card.getRarity(), new int[]{50, 70});
        int base = new Random().nextInt(range[1] - range[0] + 1) + range[0];
        base = isDiscounted ? (int)(base * 0.8) : base;
        base = isHalfPrice ? (int)(base * 0.5) : base;
        return base;
    }
    public static int getRandomPriceForRelic(boolean isHalfPrice) {
        int price = new Random().nextInt(31) + 90; // 90 ~ 120
        return isHalfPrice ? (int)(price * 0.5) : price;
    }
    public static int getPriceForShopService(int price, boolean isHalfPrice) {
        return isHalfPrice ? (int)(price * 0.5) : price;
    }
}
