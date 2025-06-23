package com.zixun.cardGame.type;

public enum NodeContentEnum {
    NONE("[]"),
    START_EVENT("起点"),
    ENEMY("敌人"),
    MINI_BOSS("精英"),
    FINAL_BOSS("关底Boss"),
    TREASURE("宝物"),
    REST("休息点"),
    EVENT("事件"),
    SHOP("商店");

    private final String name;

    NodeContentEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
