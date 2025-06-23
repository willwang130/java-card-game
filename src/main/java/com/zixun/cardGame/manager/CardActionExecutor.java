package com.zixun.cardGame.manager;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.behavior.CardHandlerChain;
import com.zixun.cardGame.behavior.StrategyMap;
import com.zixun.cardGame.behavior.strategy.*;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.card.CardLevelData;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.util.Log;

import java.util.Map;

public class CardActionExecutor implements CardHandlerChain {

    @Override
    public CardEffectResult apply(Card card, Character player, Character monster) {
        CardLevelData data = card.getCurrentLevelData();
        Map<String, Object> action = data.action;

        boolean killed = false;
        CardEffectResult finalResult =  null;

        for (Map.Entry<String, Object> entry : action.entrySet()) {
            String key = entry.getKey();
            if (key.equals("onKill")) continue;

            CardEffectStrategy strategy = StrategyMap.get(key);
            if (strategy == null) {
                Log.write("[未知卡牌行为类型: " + key + "]");
                continue;
            }
            Character target = monster;                  // 默认打向敌人
            if (("applyBuff".equals(key) || "applyDebuff".equals(key))
                    && Boolean.TRUE.equals(action.get("applyToSelf"))) {
                target = player;                         // 写了 applyToSelf:true → 改打自己
            }
            CardEffectResult result = strategy.execute(
                    entry.getValue(), card, player, target, GameEngine.getInstance());


            if (result.monsterKilled()) {
                killed = true;
            }

            if (!result.success()) {
                finalResult = result;
            }
        }
        // 如果击杀了，执行 onKill
        if (killed && action.containsKey("onKill")) {
            CardEffectStrategy strategy = StrategyMap.get("onKill");
            Object value = action.get("onKill");
            strategy.execute(value, card, player, monster, GameEngine.getInstance());
        }

        if (killed) return CardEffectResult.Killed("敌人被击杀!");
        return finalResult != null ? finalResult : CardEffectResult.success("");
    }

    public CardEffectStrategy getStrategy(String key) {
        return StrategyMap.get(key);
    }
}
