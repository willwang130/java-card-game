package com.zixun.cardGame.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

public class MonsterDataLoader {
    private static Map<String, Map<String, Object>> monsterMapAll;

    public static void loadAll(String jsonFileName) {
        try (InputStream is = MonsterDataLoader.class.getResourceAsStream("/" + jsonFileName)) {
            //System.out.println("[调试] 使用 Class.getResourceAsStream：路径 = /" + jsonFileName);
            if (is == null) throw new RuntimeException("找不到配置文件: " + jsonFileName);

            ObjectMapper mapper = new ObjectMapper();
            monsterMapAll = mapper.readValue(
                    is, new TypeReference<Map<String, Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("加载属性失败: " + jsonFileName, e);
        }
        System.out.println("Loaded monsterMapAll: " + monsterMapAll);
    }

    public static Map<String, Object> getMonsterDataByName (String monsterName) {
        Map<String, Object> monsterData = monsterMapAll.get(monsterName);
        if (monsterData == null) throw new RuntimeException("未知怪物名: " + monsterName);
        return monsterData;
    }

    public static Map<String, Map<String, Object>> getMonsterMapAll() {
        return monsterMapAll;
    }
}
