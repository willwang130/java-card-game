package com.zixun.cardGame.behavior;

import com.zixun.cardGame.behavior.strategy.*;

import java.util.Map;

public class StrategyMap {
    private static final Map<String, CardEffectStrategy> STRATEGY_MAP = Map.ofEntries(
            Map.entry("damage", new DamageStrategy()),
            Map.entry("draw", new DrawStrategy()),
            Map.entry("gainBlock", new GainBlockStrategy()),
            Map.entry("applyDebuff", new ApplyDebuffStrategy()),
            Map.entry("addBuff", new AddBuffStrategy()),
            Map.entry("addBuffThisBattle", new AddBuffThisBattleStrategy()),
            Map.entry("copyCard", new CopyCardStrategy()),
            Map.entry("gainBuffThisTurn", new GainBuffThisTurnStrategy()),
            Map.entry("gainEp", new GainEpStrategy()),
            Map.entry("loseHp", new LoseHpStrategy()),
            Map.entry("exhaust", new ExhaustCardStrategy()),
            Map.entry("multiHit", new MultiHitStrategy()),
            Map.entry("damageEqualsCurrentBlock", new DamageEqualsBlockStrategy()),
            Map.entry("damageRandomEnemy", new DamageRandomEnemyStrategy()),
            Map.entry("requireAllAttackInHand", new RequireAllAttackStrategy()),
            Map.entry("doubleCurrentBlock", new DoubleBlockStrategy()),
            Map.entry("damageOnKill", new DamageAndOnKillStrategy()),
            Map.entry("addRandomCard", new AddRandomCardStrategy()),
            Map.entry("playTopCardAndExhaust", new PlayTopCardAndExhaustStrategy()),
            Map.entry("xCost", new XCostDamageStrategy()),
            Map.entry("lifeSteal", new LifeStealStrategy()),
            Map.entry("blockRetention", new BlockRetentionStrategy()),
            Map.entry("multiplyBuffThisBattle", new MultiplyBuffStrategy()),
            Map.entry("all_enemies", new AllEnemiesStrategy()),
            Map.entry("trigger", new TriggerEffectStrategy()),
            Map.entry("thornsThisTurn", new GainThornsThisTurnStrategy()),
            Map.entry("incrementalDamagePerUse", new incrementalDamagePerUseStrategy()),
            Map.entry("addThorns", new GainThornsStrategy())
    );
    public static CardEffectStrategy get(String effectType) {
        return STRATEGY_MAP.get(effectType);
    }

    public static boolean contains(String effectType) {
        return STRATEGY_MAP.containsKey(effectType);
    }

    public static Map<String, CardEffectStrategy> getAll() {
        return STRATEGY_MAP;
    }
}
