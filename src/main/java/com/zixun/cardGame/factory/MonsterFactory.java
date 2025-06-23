package com.zixun.cardGame.factory;

import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.util.MonsterDataLoader;

import java.util.*;
import java.util.function.Predicate;

public class MonsterFactory {
    private static final Map<String, Map<String, Object>> allMonsterData;
    private static final Random random = new Random();

    static {
        allMonsterData = MonsterDataLoader.getMonsterMapAll();
    }

    public static Monster getRandomNormal(int layer) {
        return getRandomByFilter(entry ->
                "normal".equals(entry.get("type"))
        );
    }

    public static Monster getRandomElite(int layer) {
        return getRandomByFilter(entry -> {
            Object typeObj = entry.get("type");
            return typeObj instanceof String typeStr && typeStr.equals("elite_" + layer);
        });
    }

    public static Monster getBoss(int layer) {
        return getRandomByFilter(entry ->
                ("boss_" + layer).equals(entry.get("type"))
        );
    }

    private static Monster getRandomByFilter(Predicate<Map<String, Object>> filter) {
        List<String> candidates = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : allMonsterData.entrySet()) {
            if (filter.test(entry.getValue())) {
                candidates.add(entry.getKey());
            }
        }
        if (candidates.isEmpty()) {
            throw new RuntimeException("没有符合的怪物！");
        }
        String name = candidates.get(random.nextInt(candidates.size()));
        return Monster.fromMap(name, allMonsterData.get(name));
    }
}