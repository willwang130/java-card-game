package com.zixun.cardGame.type;

public enum CardViewSize {
    BATTLE(100, 160),
    BATTLE_HOVER(120, 190),

    DISPLAY_MEDIUM(120, 190),        // 商店列表
    DISPLAY_LARGE(130, 220),         // 铸造、弃牌堆、抽牌堆等
    DISPLAY_XL(280, 400);            // 升级预览页

    public final int width;
    public final int height;

    CardViewSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
