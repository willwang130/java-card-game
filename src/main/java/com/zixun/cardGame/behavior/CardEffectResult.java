package com.zixun.cardGame.behavior;

import com.zixun.cardGame.util.Log;

public record CardEffectResult(boolean success, boolean monsterKilled) {

    public static CardEffectResult success(String... log) {
        writeIfPresent(log);
        return new CardEffectResult(true, false);
    }

    public static CardEffectResult Killed(String... log) {
        writeIfPresent(log);
        return new CardEffectResult(true, true);
    }

    public static CardEffectResult failed(String... log) {
        writeIfPresent(log);
        return new CardEffectResult(false, false);
    }

    private static void writeIfPresent(String... log) {
        if (log != null) {
            for (String s : log) {
                if (s != null && !s.isBlank()) {
                    Log.write(s);
                }
            }
        }
    }
}
