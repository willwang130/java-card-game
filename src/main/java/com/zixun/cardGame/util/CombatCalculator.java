package com.zixun.cardGame.util;
import com.zixun.cardGame.manager.StatusManager;
import com.zixun.cardGame.model.character.Character;

import static com.zixun.cardGame.type.StatusNames.*;

public class CombatCalculator {

    public static int dealDamageWithCalc(Character caster, Character target, int baseDamage) {
        int finalDamage = calculateFinalDamage(caster, target, baseDamage);
        return applyDamage(caster, target, finalDamage, false); // false：伤害来源于另一个 character, true: 来源于反伤等
    }

    // 1. 计算最终伤害 力量/虚弱/易伤影响，但不处理护甲和扣血
    public static int calculateFinalDamage(Character caster, Character target, int baseDamage) {
        int result = baseDamage;

        var casterStatus = caster.getStatusManager();

        result += casterStatus.get(STRENGTH);

        if (casterStatus.has(WEAK)) {
            result = (int) Math.floor(result * 0.75);
        }

        var targetStatus = target.getStatusManager();
        if (targetStatus.has(VULNERABLE)) {
            result = (int) Math.floor(result * 1.5);
        }

        return Math.max(0, result);
    }

    // 2. 加持各类 护甲, debuff, 宝物等的最终伤害, 并扣减HP, 护甲
    public static int applyDamage(Character caster, Character target, int damage, boolean isThornsDamage) {
        if (damage <= 0) return 0;
        // 护甲逻辑
        int oldBlock = target.getBlock();
        int blocked = Math.min(damage, oldBlock);
        int actual = damage - blocked;
        target.setBlock(oldBlock - blocked);

        // 扣血
        target.addHp(-actual);

        if (!isThornsDamage && caster != null && caster != target) {
            int thorns = target.getStatusManager().getTotalThorns();
            if (thorns > 0) {
                Log.write(caster.getName() + " 受到 " + thorns + " 点反伤！");
                applyDamage(target, caster, thorns, true);
            }
        }

        return actual;
    }

    // 根据力量, 虚弱修正
    public static int calcAttack(int base, Character caster) {
        StatusManager statusManager = caster.getStatusManager();

         base += statusManager.get(STRENGTH);

        if (statusManager.has(WEAK)) {
            base = (int)(base * 0.75);
        }

        return base;
    }

    // 根据敌人易伤修正
    public static int calcAttackToTarget(int mod, Character target) {
        StatusManager statusManager = target.getStatusManager();
        if (statusManager.has(VULNERABLE)) {
            mod = (int)(mod * 1.5);
        }
        return mod;
    }

    public static int calcBlock(int base, Character caster) {
        StatusManager statusManager = caster.getStatusManager();

        base += statusManager.get(DEXTERITY);

        if (statusManager.has(FRAIL)) {
            base = (int)(base * 0.85);
        }
        return base;
    }
}
