package com.zixun.cardGame.util;

public class EnumUtil {
    public static <T extends Enum<T>> T safeEnum(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
