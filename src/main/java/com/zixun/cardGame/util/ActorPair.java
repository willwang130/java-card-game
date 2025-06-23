package com.zixun.cardGame.util;

import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.model.character.Monster;

public class ActorPair {
    public final String casterName;
    public final String targetName;

    public ActorPair(Character caster, Character target) {
        this.casterName = (caster instanceof Monster) ? "[MONSTER] " + caster.getName() : "[PLAYER] 你";
        this.targetName = (target instanceof Monster) ? "[MONSTER] " +target.getName() : "[PLAYER] 你";
    }
}