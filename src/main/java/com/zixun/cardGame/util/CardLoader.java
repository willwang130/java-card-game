package com.zixun.cardGame.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zixun.cardGame.model.card.Card;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardLoader {
    private static final Map<String, Card> cardAll = new HashMap<>();

    public static void loadAll(String jsonFileName) {
        try (InputStream is = CardLoader.class.getResourceAsStream("/" + jsonFileName)) {
            if (is == null) throw new RuntimeException("找不到配置文件: " + jsonFileName);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rawMap = mapper.readValue(is, new TypeReference<Map<String, Object>>() {});

            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                String cardName = entry.getKey();
                Map<String, Object> cardDataMap = (Map<String, Object>) entry.getValue();

                Card card = Card.fromMap(cardName, cardDataMap);
                cardAll.put(cardName, card);
            }

        } catch (Exception e) {
            throw new RuntimeException("加载卡牌失败: " + jsonFileName, e);
        }
    }
    public static Map<String, Card> getALLCards() {
        return cardAll;
    }
}
