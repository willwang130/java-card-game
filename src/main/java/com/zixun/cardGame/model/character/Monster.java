package com.zixun.cardGame.model.character;

import com.zixun.cardGame.behavior.MonsterAction;
import com.zixun.cardGame.observer.Observer;
import com.zixun.cardGame.type.StatusNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Monster extends Character{
    private final String type;
    private final List<MonsterAction> aiScript;
    private int currentAiIndex = 0;

    private final List<Observer<Monster>> observerList = new ArrayList<>();

    public Monster(String name, int maxHp, int ep, String type, List<MonsterAction> ai
            , Map<StatusNames, Integer> buffs) {
        super(name, maxHp, ep);
        this.type = type;
        this.aiScript = ai;
        if (buffs != null) {
            buffs.forEach((k, v) -> statusManager.addBuff(k, v));
        }
    }

    public MonsterAction getNextAction() {
        MonsterAction action = aiScript.get(currentAiIndex);
        currentAiIndex = (currentAiIndex + 1) % aiScript.size();
        return action;
    }

    public static Monster fromMap(String name, Map<String, Object> data) {
        int hp = (int) data.getOrDefault("hp",50);
        String type = (String) data.getOrDefault("type", "normal");
        // 初始buffs
        Map<StatusNames, Integer> buffs = new HashMap<>();
        Map<String, Object> rawBuffs = (Map<String, Object>) data.getOrDefault("initialBuffs", new HashMap<>());
        for (Map.Entry<String, Object> entry : rawBuffs.entrySet()) {
            if (entry.getValue() instanceof Number n) {
                buffs.put(StatusNames.getStatusFromJson(entry.getKey()), n.intValue());
            }
        }
        // AI 脚本解析
        List<MonsterAction> aiScript = new ArrayList<>();
        List<Map<String, Object>> aiRow = (List<Map<String, Object>>) data.getOrDefault("ai", new ArrayList<>());
        aiScript = MonsterAction.parseList(aiRow);
        // 构建Monster
        return new Monster(name, hp, 0, type, aiScript, buffs);
    }

    public void addObserver(Observer<Monster> observer) {
        observerList.add(observer);
    }

    public void removeObserver(Observer<Monster> observer) {
        observerList.remove(observer);
    }

    public void notifyObservers() {
        List<Observer<Monster>> copy = new ArrayList<>(observerList);
        for (Observer<Monster> observer : copy) {
            observer.onChange(this);
        }
    }

    public MonsterAction peekNextAction() {
        return aiScript.get(currentAiIndex);
    }
}
