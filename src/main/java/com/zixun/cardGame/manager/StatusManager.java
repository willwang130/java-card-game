package com.zixun.cardGame.manager;

import com.zixun.cardGame.type.StatusNames;

import java.util.*;

public class StatusManager {
    // DeBuff 回合数倒计类：vulnerable, weak, frail
    private final Map<StatusNames, Integer> deBuffMap = new HashMap<>();
    // Buff 数值叠加类：strength, dexterity
    private final Map<StatusNames, Integer> buffMap = new HashMap<>();
    // 一回合临时类
    private final Map<StatusNames, Integer> oneTurnBuffMap = new HashMap<>();
    // 一场战斗临时类
    private final Map<StatusNames, Integer> oneBattleBuffMap = new HashMap<>();

    private final List<Runnable> listeners = new ArrayList<>();

    private int permanentThorns = 0;
    private int tempThorns = 0;

    public void addDebuff(StatusNames name, int turns) {
        deBuffMap.put(name, deBuffMap.getOrDefault(name, 0) + turns);
    }

    public void setDebuff(StatusNames name, int turns) {
        if (turns > 0) deBuffMap.put(name, turns);
        else deBuffMap.remove(name);
    }

    public int getDebuffDuration(StatusNames name) {
        return deBuffMap.getOrDefault(name, 0);
    }

    public boolean hasDebuff(StatusNames name) {
        return getDebuffDuration(name) > 0;
    }

    public void clearDebuff(StatusNames name) {
        deBuffMap.remove(name);
    }

    public void clearAllDebuffs() {
        deBuffMap.clear();
    }

    public void decayDebuffs() {
        List<StatusNames> toRemove = new ArrayList<>();
        for (StatusNames key : deBuffMap.keySet()) {
            int newVal = deBuffMap.get(key) - 1;
            if (newVal <= 0) toRemove.add(key);
            else deBuffMap.put(key, newVal);
        }
        toRemove.forEach(deBuffMap::remove);
    }

    public void addBuff(StatusNames name, int amount) {
        buffMap.put(name, buffMap.getOrDefault(name, 0) + amount);
        notifyAllListeners();
    }

    public void setBuff(StatusNames name, int value) {
        if (value != 0) buffMap.put(name, value);
        else buffMap.remove(name);
        notifyAllListeners();
    }

    public int getBuffValue(StatusNames name) {
        return buffMap.getOrDefault(name, 0);
    }

    public boolean hasBuff(StatusNames name) {
        return getBuffValue(name) > 0;
    }

    public void clearBuff(StatusNames name) {
        buffMap.remove(name);
    }

    public void clearAllBuffs() {
        buffMap.clear();
    }

    public Map<StatusNames, Integer> getActiveBuffs() {
        return Collections.unmodifiableMap(buffMap);
    }

    public Map<StatusNames, Integer> getActiveDebuffs() {
        return Collections.unmodifiableMap(deBuffMap);
    }

    public void addOneTurnBuff(StatusNames name, int amount) {
        oneTurnBuffMap.put(name, oneTurnBuffMap.getOrDefault(name, 0) + amount);
    }
    public int getOneTurnBuffValue(StatusNames name) {
        return oneTurnBuffMap.getOrDefault(name, 0);
    }
    public boolean hasOneTurnBuff(StatusNames name) {
        return getOneTurnBuffValue(name) > 0;
    }
    public void clearOneTurnBuffs() {
        oneTurnBuffMap.clear();
    }  // 每回合结束调用

    public void addOneBattleBuff(StatusNames name, int amount) {
        oneBattleBuffMap.put(name, oneBattleBuffMap.getOrDefault(name, 0) + amount);
    }
    public int getOneBattleBuffValue(StatusNames name) {
        return oneBattleBuffMap.getOrDefault(name, 0);
    }
    public boolean hasOneBattleBuff(StatusNames name) {
        return getOneBattleBuffValue(name) > 0;
    }
    public void clearOneBattleBuffs() {
        oneBattleBuffMap.clear();
    }  // 每场游戏开始调用

    public int get(StatusNames s) {
        int perm  = buffMap.getOrDefault(s, 0) + deBuffMap.getOrDefault(s, 0);
        int turn  = oneTurnBuffMap.getOrDefault(s, 0);
        int battle = oneBattleBuffMap.getOrDefault(s, 0);
        return perm + turn + battle;
    }
    public boolean has(StatusNames s) {
        return get(s) > 0;
    }

    public void addPermanentThorns(int value) {
        this.permanentThorns += value;
    }

    public void addTempThorns(int value) {
        this.tempThorns += value;
    }

    public int getTotalThorns() {
        return permanentThorns + tempThorns;
    }

    public void clearTempThorns() {
        this.tempThorns = 0;
    }

    public void addListener(Runnable a) { listeners.add(a); }
    private void notifyAllListeners() { listeners.forEach(Runnable::run); }

    public Map<StatusNames, Integer> getAll() {
        Map<StatusNames, Integer> combined = new LinkedHashMap<>();

        // ① 正面 Buff
        buffMap.forEach(combined::put);

        // ② 负面 Debuff
        deBuffMap.forEach(combined::put);

        // ③ 临时 Buff（叠加数值）
        oneTurnBuffMap.forEach((status, val) ->
                combined.merge(status, val, Integer::sum));

        oneBattleBuffMap.forEach((status, val) ->
                combined.merge(status, val, Integer::sum));


        return Collections.unmodifiableMap(combined);
    }
}
