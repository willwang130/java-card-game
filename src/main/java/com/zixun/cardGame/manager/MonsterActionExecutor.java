package com.zixun.cardGame.manager;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.behavior.MonsterAction;
import com.zixun.cardGame.behavior.StrategyMap;
import com.zixun.cardGame.behavior.strategy.CardEffectStrategy;
import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.util.CombatCalculator;

import java.util.Map;

public class MonsterActionExecutor {

    public static String executeMonsterAction(MonsterAction action,
                                              Monster monster, Player target) {
        StringBuilder sb = new StringBuilder();

        Map<String, Object> actionMap = action.getActionMap();
        for (Map.Entry<String, Object> entry : actionMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            CardEffectStrategy strategy = StrategyMap.get(key);
            if (strategy != null) {
                CardEffectResult result = strategy.execute(value, null, monster, target, GameEngine.getInstance());
//                if (result.success()) {
//                    sb.append("[").append(monster.getName()).append("执行").append(key).append("成功] \n");
//                } else {
//                    sb.append("[").append(key).append("失败] \n");
//                }
            } else {
                sb.append("[MONSTER] [未知行为类型: ").append(key).append("] \n");
            }
        }
        return sb.toString();
    }
}
