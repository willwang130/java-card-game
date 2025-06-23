package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.type.TriggerTypes;

import java.util.Map;

public class TriggerEffectStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value,
                                    Card card,
                                    Character caster,
                                    Character target,
                                    GameEngine engine) {
        // 解析 json
        if (!(value instanceof Map<?, ?> triggerObj)) {
            return CardEffectResult.failed("json: 无效的 trigger");
        }
        String triggerType = (String) triggerObj.get("type");
        Map<String, Object> effectMap = (Map<String, Object>) triggerObj.get("effect");

        if (triggerType == null || effectMap == null) {
            return CardEffectResult.failed("触发器格式错误");
        }

        // 识别来源
        String sourceId;
        SourceType sourceType;

        if (card != null) {
            sourceId = card.getName();
            sourceType = SourceType.CARD;
            if (card.getType().equals("能力")) {
                caster.addAbility(card);
            }
        } else if (caster != null) {
            sourceId = caster.getInstanceId();
            sourceType = (caster instanceof Player) ? SourceType.CARD : SourceType.MONSTER;
        } else {
            sourceId = "GLOBAL";
            sourceType = SourceType.GLOBAL;
        }

        engine.getTriggerManager()
                .registerOrMergeTrigger(sourceId, SourceType.CARD, triggerType, effectMap);
        return CardEffectResult.success("已注册Trigger:" + triggerType + " " + sourceType);
    }
}
