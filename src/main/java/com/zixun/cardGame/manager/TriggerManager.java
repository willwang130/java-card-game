package com.zixun.cardGame.manager;

import com.zixun.cardGame.behavior.StrategyMap;
import com.zixun.cardGame.behavior.strategy.CardEffectStrategy;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriggerManager {
    private static final TriggerManager INSTANCE = new TriggerManager();

    public static TriggerManager getInstance() {
        return INSTANCE;
    }

    private TriggerManager() {}

    private final List<Map<String, Object>> passiveEffects = new ArrayList<>();

    public void trigger(String triggerType, Character triggerCaster, Runnable finishGameIfNeeded) {
        System.out.println("🟡 TriggerManager: 尝试触发 " + triggerType);
        for (Map<String, Object> entry : passiveEffects) {
            String registeredTrigger = (String) entry.get("trigger");
            if (!triggerType.equals(registeredTrigger)) continue;

            Object effectObj = entry.get("effect");
            if (!(effectObj instanceof Map)) continue;

            String source = (String) entry.getOrDefault("source", "未知来源");
            System.out.println("🟢 触发来自 [" + source + "] 的效果");

            Map<String, Object> effectMap = (Map<String, Object>) effectObj;
            for (Map.Entry<String, Object> effectEntry : effectMap.entrySet()) {
                String strategyJson = effectEntry.getKey();
                Object value = effectEntry.getValue();

                CardEffectStrategy strategy = StrategyMap.get(strategyJson);
                if (strategy != null) {
                    System.out.println("    ⏩ 执行策略 [" + strategyJson + "] 值 = " + value);
                    Character target = triggerCaster instanceof Player ? GameEngine.getInstance().getMonster() : triggerCaster;
                    strategy.execute(value, null, triggerCaster
                            , target, GameEngine.getInstance());
                } else {
                    System.out.println("    ⚠ 未知策略类型: " + strategyJson);
                }
            }
        }
        finishGameIfNeeded.run();
    }


    public void registerOrMergeTrigger(
            String sourceId,
            SourceType sourceType,
            String triggerType,
            Map<String, Object> newEffect
    ) {
        for (Map<String, Object> entry : passiveEffects) {
            //  同一种 trigger 合并数值
            String existingTrigger = (String) entry.get("trigger"); // StarTurn 等
            String existingSource = (String) entry.get("source");

            if (triggerType.equals(existingTrigger) && sourceId.equals(existingSource)) {
                Object effectObj = entry.get("effect");
                if (effectObj instanceof Map<?, ?>) {
                    Map<String, Object> existingEffect = (Map<String, Object>) effectObj;
                    for (Map.Entry<String, Object> effectEntry : newEffect.entrySet()) {
                        String key = effectEntry.getKey();
                        Object value = effectEntry.getValue();
                        if (value instanceof Number v) {
                            int old = ((Number) existingEffect.getOrDefault(key, 0)).intValue();
                            existingEffect.put(key, old + v.intValue());
                        } else {
                            existingEffect.put(key, value);
                        }
                    }
                }
                return;
            }
        }
        // 没有旧 trigger，正常注册
        Map<String, Object> triggerEntry = Map.of(
                "source", sourceId,
                "sourceType", sourceType.name(),
                "trigger", triggerType,
                "effect", new HashMap<>(newEffect)
        );
        passiveEffects.add(triggerEntry);
    }

    public void clearStartingBattle() {

    }

    public void clearPassiveEffects(String sourceId) {
        passiveEffects.removeIf(effect -> sourceId.equals(effect.get("source")));
    }
    public void clearPassiveEffectsByType(SourceType type) {
        passiveEffects.removeIf(effect -> type.name().equals(effect.get("sourceType")));
    }
    public void clearAll() {
        passiveEffects.clear();
    }
}
