package com.zixun.cardGame.model.character;
import com.zixun.cardGame.manager.StatusManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.type.ActiveAbility;
import com.zixun.cardGame.type.CharacterTypeEnum;

import java.util.*;

public abstract class Character {
    private String instanceId;
    protected String name;
    protected int maxHp, maxEp, hp, ep;
    protected int block;
    protected boolean blockRetention;
    protected StatusManager statusManager = new StatusManager();
    protected abstract void notifyObservers();

    private final Map<String, ActiveAbility> activeAbilities = new LinkedHashMap<>();

    // 特殊状态
    private int artifactCount = 0; // 抵挡负面状态
    private boolean intangibleActive = false;  //所受伤害不超过 1 影响所有攻击


    public Character(String name, int maxHp, int maxEp) {
        this.instanceId = UUID.randomUUID().toString();
        this.name = name;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.ep = maxEp;
        this.maxEp = maxEp;
        this.block = 0;
        blockRetention = false;
    }

    public void healCharacter(int amount) {
        hp = Math.min(hp + amount, maxHp);
        notifyObservers();
    }

    public void addAbility(Card powerCard) {
        activeAbilities.putIfAbsent(powerCard.getName(),
                new ActiveAbility(powerCard.getName(),
                        powerCard.getDescription()
                ));
    }

    public Collection<ActiveAbility> getActiveAbilities() {
        return activeAbilities.values();
    }

    public void resetAbility() {
        activeAbilities.clear();
    }

    public void resetBlock() {
        this.block = 0;
    }

    // 加减属性 + 通知 observer
    public void addHp(int amount) {
        hp = Math.min(hp + amount, maxHp); notifyObservers();
    }

    public void addMaxHp(int x) {
        maxHp += x; notifyObservers();
        hp = Math.min(hp, maxHp);
    }

    public void addEp(int amount) { ep += amount; notifyObservers(); }
    public void minusEp(int amount) { ep -= amount; notifyObservers(); }
    public void addMaxEp(int x) { maxEp += x; notifyObservers(); }
    public void addBlock(int x) { block += x; notifyObservers(); }

    // getters
    public String getInstanceId() { return instanceId; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getEp() { return ep; }
    public int getMaxHp() { return maxHp; }
    public int getMaxEp() { return maxEp; }
    public int getBlock() { return block; }
    public StatusManager getStatusManager() { return statusManager; }
    public boolean isBlockRetention() { return blockRetention; }

    // setters
    public void setBlock(int block) { this.block = block; }
    public void setBlockRetention(boolean value) { this.blockRetention = value; }

}
