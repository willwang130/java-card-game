package com.zixun.cardGame.factory;

import com.zixun.cardGame.model.relic.*;

import java.util.*;

public class RelicFactory {

    public static final Random random = new Random();
    public static final Map<String, Relic> RELIC_SUPPLIER = new HashMap<>();
    public static final int RELIC_IN_TOTAL;

    static {
        put(new BANDAGE());
        put(new BERSERK_AMULET());
        put(new GUARD_EMBLEM());
        put(new WEAK_HORN());
        put(new THORNS_AMULET());
        put(new SMALL_POUCH());
        RELIC_IN_TOTAL = RELIC_SUPPLIER.size();
    }

    public static List<Relic> getRandomRelicsFromUnowned(int count, List<Relic> owned) {
        List<Relic> all = new ArrayList<>(getAllRelic());
        List<Relic> unowned = new ArrayList<>(all);
        unowned.removeAll(owned);

        Collections.shuffle(unowned);
        List<Relic> result = new ArrayList<>();

        int pickCount = Math.min(count, unowned.size());
        result.addAll(unowned.subList(0, pickCount));

        if (result.size() < count) {
           List<Relic> ownedCopy = new ArrayList<>(owned);
           Collections.shuffle(ownedCopy);
           int remaining = count - result.size();
           result.addAll(ownedCopy.subList(0, Math.min(remaining, ownedCopy.size())));
        }
        return result;
    }


    public static Relic randomRelic() {
        // 从 values() 里随机挑选
        List<Relic> list = new ArrayList<>(RELIC_SUPPLIER.values());
        return list.get(random.nextInt(list.size()));
    }

    public static List<Relic> getAllRelic() {
        return RELIC_SUPPLIER.values().stream().toList();
    }

    private static void put(Relic relic) {
        RELIC_SUPPLIER.put(relic.getName(), relic);
    }
}