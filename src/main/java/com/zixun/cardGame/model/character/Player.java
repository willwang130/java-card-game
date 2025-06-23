package com.zixun.cardGame.model.character;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.observer.Observer;
import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.factory.RelicFactory;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character{
    private final List<Observer<Player>> observerList = new ArrayList<>();

    private final List<Relic> relicList = new ArrayList<>(20);

    private int gold = 0;
    private boolean isHalfPrice;

    public Player (String name) {
        super(name, 80, 3);
    }

    public void addObserver(Observer<Player> observer) {
        observerList.add(observer);
    }

    public void removeObserver(Observer<Player> observer) {
        observerList.remove(observer);
    }

    public void notifyObservers() {
        List<Observer<Player>> copy = new ArrayList<>(observerList);
        for (Observer<Player> observer : copy) {
            observer.onChange(this);
        }
    }

    public boolean addAndEquipRelic(Relic relic) {
        if (relic == null) return false;
        if (relicList.size() >= RelicFactory.RELIC_IN_TOTAL) return false;
        if (relicList.contains(relic)) { return false; }
        relicList.add(relic);
        relic.onAcquired(this, GameEngine.getInstance()); // TODO:
        return true;
    }

    public Relic addNewRandomRelic() {
        Relic randomRelic = null;
        do {
            randomRelic = RelicFactory.randomRelic();
        } while (relicList.contains(randomRelic) && relicList.size() < RelicFactory.RELIC_IN_TOTAL);
        return addAndEquipRelic(randomRelic) ? randomRelic : null;
    }

    public void refillEp() {
        ep = maxEp;
        notifyObservers();
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public List<Relic> getRelicList() {
        return relicList;
    }

    public boolean isHalfPrice() { return isHalfPrice; }
    public void setShopDiscount(boolean discount) { this.isHalfPrice = discount; }

}
