package com.zixun.cardGame.manager;

import com.zixun.cardGame.map.Level;
import com.zixun.cardGame.map.MapNode;

import java.util.ArrayList;
import java.util.List;

import static com.zixun.cardGame.type.NodeContentEnum.*;
import static com.zixun.cardGame.type.NodeTypeEnum.BOSS;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private final List<Level> levels = new ArrayList<>();
    private final int TOTAL_LEVELS = 3;
    private int currentLevelIdex = 0;
    private MapNode battleStartNode;

    private GameManager() {};

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public void initializeGripGameMap(int totalLevels) {
        levels.clear();
        MapNode preBoss = null;
        for (int i = 1; i <= totalLevels; i++) {
                Level level = new Level(i, preBoss);
                preBoss = level.getBossNode();
                levels.add(level);
        }
    }
    public void initializeLayeredGameMap(int totalLevels) {
        levels.clear();
        for (int i = 1; i <= totalLevels; i++) {
            Level level = new Level(i);
            levels.add(level);
        }
    }

    public boolean moveToNextLevel() {
        if (currentLevelIdex < levels.size() - 1) {
            currentLevelIdex++;
            return true;
        }
        return false;
    }

    public boolean isGameCompleted() {
        return currentLevelIdex >= levels.size() - 1;
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIdex);
    }

   public int getCurrentLevelNumber() {
        return levels.get(currentLevelIdex).getLevelNumber();
   }

    public boolean hasMoreLevel() {
        return currentLevelIdex < levels.size() - 1;
    }

    public void resetToGripMap() {
        currentLevelIdex = 0;
        battleStartNode = null;
        initializeGripGameMap(TOTAL_LEVELS);
    }
    public void resetToLayeredGameMap() {
        currentLevelIdex = 0;
        battleStartNode = null;
        initializeLayeredGameMap(TOTAL_LEVELS);
    }
    public MapNode getBattleStartNode() {
        return battleStartNode;
    }

    public void setBattleStartNode(MapNode currentStandingNode) {
        this.battleStartNode = currentStandingNode;
    }

    public void clearNodeAfterBattle() {
        MapNode playerNode = getCurrentLevel().getGameMap().getPlayerNode();
        if (playerNode.content == ENEMY || playerNode.content == MINI_BOSS || playerNode.type == BOSS) {
            playerNode.monster = null;
            playerNode.content = NONE;
        }
    }

    public int getLevelsSize() {
        return levels.size();
    }
}
