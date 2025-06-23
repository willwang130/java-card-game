package com.zixun.cardGame.type;

import java.util.Map;

public enum StatusNames {
    // Status
    STRENGTH,WEAK,VULNERABLE,DEXTERITY, FRAIL, DRAW_BONUS
    ,BLOCK_PER_TURN,ATTACK_BONUS,KILL_REWARD,
    // Relics
    GUARD_EMBLEM, BERSERK_AMULET, WEAK_HORN,
    SMALL_POUCH, BANDAGE, THORNS_AMULET;

    // 伤害计算用
    public static final Map<String, StatusNames> CARD_JSON_TO_STATUS = Map.of(
            "strength", STRENGTH,
            "weak", WEAK,
            "vulnerable", VULNERABLE,
            "frail", FRAIL,
            "dexterity", DEXTERITY
    );

    // UI显示用
    public static final Map<StatusNames, String> STATUS_TO_CHINESE = Map.ofEntries(
            Map.entry(STRENGTH, "力量"),
            Map.entry(WEAK, "虚弱"),
            Map.entry(VULNERABLE, "易伤"),
            Map.entry(FRAIL, "虚弱防御"),
            Map.entry(DEXTERITY, "敏捷"),
            // Relics
            Map.entry(GUARD_EMBLEM, "守护徽章"),
            Map.entry(BERSERK_AMULET, "狂战护符"),
            Map.entry(WEAK_HORN, "虚弱号角"),
            Map.entry(SMALL_POUCH, "小口袋"),
            Map.entry(BANDAGE, "绷带"),
            Map.entry(THORNS_AMULET, "荆棘护符")
    );
    public static final Map<StatusNames, String> deBuffs = Map.ofEntries(
            Map.entry(WEAK, "虚弱"),
            Map.entry(VULNERABLE, "易伤"),
            Map.entry(FRAIL, "虚弱防御")
    );

    private static final Map<StatusNames,String> DESC = Map.of(
            STRENGTH   ,"提高造成的伤害",
            WEAK       ,"造成的伤害减少 25%",
            VULNERABLE ,"受到的伤害增加 50%",
            FRAIL      ,"格挡效果减半",
            DEXTERITY  ,"提高获得格挡的数值"
    );

    public static String getDescription(StatusNames s) {
        return DESC.getOrDefault(s, "");
    }



    public static StatusNames getStatusFromJson(String key) {
        return CARD_JSON_TO_STATUS.get(key);
    }
    public static String getChineseFromStatus(StatusNames key) {
        return STATUS_TO_CHINESE.get(key);
    }

    public boolean isDeBuff() {
        return deBuffs.containsKey(this);
    }
}
