package com.zixun.cardGame.behavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonsterAction {
    private final Map<String, Object> actionMap;

    public MonsterAction(Map<String, Object> actionMap) {
        this.actionMap = actionMap;
    }

    public Map<String, Object> getActionMap() {
        return actionMap;
    }

    // 工厂方法, 用于从 JSON List 创建一组 MonsterAction
    public static List<MonsterAction> parseList(List<Map<String, Object>> rawList) {
        List<MonsterAction> result = new ArrayList<>();
        for (Map<String, Object> item : rawList) {
            Object rawAction = item.get("action");
            if (rawAction instanceof Map<?, ?> map) {
                result.add(new MonsterAction((Map<String, Object>) map));
            } else {
                throw new IllegalArgumentException("JSON 未包含 action 字段");
            }
        }
        return result;
    }
}
