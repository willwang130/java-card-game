package com.zixun.cardGame.model.card;

import java.util.HashMap;
import java.util.Map;

public class CardLevelData {
    public int cost;
    public String description;
    public boolean exhaustAfterUse;   // 是否使用后消耗
    public boolean retainThisTurn;    // 不使用是否遗留

    public String effectType;  // 效果逻辑标识，如 "simple_attack"、"whirlwind"、"reaper"
    public Map<String, Object> action = new HashMap<>();  // 可选扩展参数（如旋风斩中额外倍率、目标等）
}
