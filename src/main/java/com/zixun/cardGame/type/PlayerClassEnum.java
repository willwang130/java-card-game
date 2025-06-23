package com.zixun.cardGame.type;

import java.util.Locale;

public enum PlayerClassEnum {
    WARRIOR("Warrior"),
    MAGE("Mage"),
    ROGUE("Rogue");

    private final String name;

    PlayerClassEnum(String name) {
        this.name = name;
    }
    public String getClassName() {
        return this.name.toLowerCase(Locale.ROOT);
    }
}
