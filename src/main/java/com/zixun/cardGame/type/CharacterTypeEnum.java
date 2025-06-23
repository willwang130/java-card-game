package com.zixun.cardGame.type;

public enum CharacterTypeEnum {
    // 括号内用于UI显示
    PLAYER("Player"),
    MONSTER("Monster"),
    BOSS("Boss"),
    UNKNOWN("Unknown"),
    //
    GOBLIN("Goblin"),
    ORC("Orc"),
    EVA("Eva");

    private final String name;

    CharacterTypeEnum(String name) {
        this.name = name;
    }
    public String getDisplayName() {
        return this.name;
    }
}
