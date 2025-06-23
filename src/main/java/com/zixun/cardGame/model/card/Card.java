package com.zixun.cardGame.model.card;

import com.zixun.cardGame.type.TargetType;

import java.util.*;

public class Card{
    private String instanceId;
    private String name;
    private String type;
    private String rarity;
    private TargetType targetType;
    private int level;
    private boolean oneTime = false;    // 临时生成的牌
    private Integer costOverrideThisTurn = null;
    private int timeUsedThisBattle = 0;

    private Map<Integer, CardLevelData> levelsMap;
    private CardLevelData currentLevelData;

    public Card() {}

//    public Card(String name, String type, String rarity, TargetType targetType,
//                Map<Integer, CardLevelData> levelsMap) {
//        this.instanceId = UUID.randomUUID().toString();
//        this.name = name;
//        this.type = type;
//        this.rarity = rarity;
//        this.targetType = targetType;
//        this.levelsMap = levelsMap;
//        loadLevel(0);
//    }

    public Card(Card card) {
        this.instanceId = UUID.randomUUID().toString();
        this.name = card.name;
        this.type = card.type;
        this.rarity = card.rarity;
        this.targetType = card.targetType;
        this.levelsMap = card.levelsMap;
        loadLevel(0);
    }

    private void loadLevel(int level) {
        this.level = level;
        this.currentLevelData = levelsMap.get(level);
    }

    public void upgrade() {
        if (getAction().containsKey("incrementalDamagePerUse")) {
           loadLevel(++level);
           return;
        }
        if (level == 0) {
            loadLevel(1);
        }
    }

    public boolean canUpgrade() {
        if (getAction().containsKey("incrementalDamagePerUse")) {
            return true;
        }
        return level == 0;
    }

    public void setCostOverrideThisTurn(Integer value) { this.costOverrideThisTurn = value; }
    public int getCost() {
        return costOverrideThisTurn != null ? costOverrideThisTurn : this.getTrueCost();
    }
    public void setCostOverrideThisTurnToNull() { costOverrideThisTurn = null; }

    public static Card fromMap(String name, Map<String, Object> map) {
        Card card = new Card();
        card.name = name;
        card.type = (String) map.getOrDefault("type", "Unknown");
        card.rarity = (String) map.getOrDefault("rarity", "Unknown");
        String targetStr = ((String) map.getOrDefault("target_type", "CENTER")).toUpperCase();
        TargetType parsed = Arrays.stream(TargetType.values())
                .filter(t -> t.name().equals(targetStr))
                .findFirst()
                .orElse(TargetType.NONE);
        card.targetType = parsed;

        Map<String, Object> level0 = (Map<String, Object>) map.getOrDefault("level_0", new HashMap<>());
        Map<String, Object> level1 = (Map<String, Object>) map.getOrDefault("level_1", new HashMap<>());

        // level_0, level_1
        Map<Integer, CardLevelData> levelsMap = new HashMap<>();
        for (int i = 0; i <= 1; i++) {
            String key = "level_" + i;
            if (map.containsKey(key)) {
                Map<String, Object> levelDataMap = (Map<String, Object>) map.get(key);

                CardLevelData data = new CardLevelData();
                data.cost = ((Number) levelDataMap.get("cost")).intValue();
                data.description = (String) levelDataMap.get("description");
                data.exhaustAfterUse = (Boolean) levelDataMap.getOrDefault("exhaust", false);
                data.retainThisTurn = (Boolean) levelDataMap.getOrDefault("retain", false);

                if (levelDataMap.containsKey("action")) {
                    data.action = (Map<String, Object>) levelDataMap.get("action");
                }
                levelsMap.put(i, data);
            }
        }
        card.levelsMap = levelsMap;
        card.loadLevel(0);
        return card;
    }

    public int extractDamageBase() {
        Map<String, Object> action = getAction();
        if (action.containsKey("damage")) return (int) action.get("damage");
        if (action.containsKey("damagePerEnergy")) return (int) action.get("damagePerEnergy");
        if (action.containsKey("lifeSteal")) return (int) action.get("lifeSteal");
        if (action.containsKey("requireAllAttackInHand")) return (int) action.get("requireAllAttackInHand");
        if (action.containsKey("incrementalDamagePerUse")) {
            return (int) action.get("baseDamage") + (timeUsedThisBattle * (int) action.get("incrementalDamagePerUse"));
        }
        if (action.containsKey("damageEqualsCurrentBlock")) return 0;
        return 0;
    }
    public int extractBlockBase() {
        Map<String, Object> action = getAction();
        if (action.containsKey("gainBlock")) return (int) action.get("gainBlock");
        return 0;
    }


    // getters
    public String getInstanceId() { return instanceId; }
    public String getName() { return name; }
    public String  getType() { return type; }
    public TargetType getTargetType() { return targetType; }
    public String getRarity() { return rarity; }
    public Map<Integer, CardLevelData> getLevelsMap() { return levelsMap; }
    public CardLevelData getCurrentLevelData() { return currentLevelData; }
    public String getEffectType() { return currentLevelData.effectType; }
    public String getDescription() { return currentLevelData.description; }
    public Map<String, Object> getParams() { return currentLevelData.action; }
    public int getTrueCost() { return currentLevelData.cost; }
    public int getLevel() { return level; }
    public boolean getExhaustAfterUse() { return currentLevelData.exhaustAfterUse; }
    public boolean getOneTime() { return oneTime; }
    public Map<String, Object> getAction() { return currentLevelData.action; }

    // setters
    public void setCost(int cost) { currentLevelData.cost = cost; }
    public void setOneTime(boolean oneTime) { this.oneTime = oneTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;
        return Objects.equals(instanceId, card.instanceId); // 唯一标识一个
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }

    public void setCostForTurn(int costThisTurn) {
        // TODO: 待改进 0耗卡牌 只为当前回合享有
        loadLevel(1);
        setCost(0);
    }

    public int getTimesUsedThisBattle() { return timeUsedThisBattle; }
    public void increaseTimeUsedThisBattle() { timeUsedThisBattle++; }
    public void resetTimeUsedThisBattle() { timeUsedThisBattle = 0; }

    public Card createUpgradedCopy() {
        Card copy = new Card(this);
        copy.upgrade();
        return copy;
    }
}
