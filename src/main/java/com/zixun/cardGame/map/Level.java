package com.zixun.cardGame.map;

public class Level{
    private final LayeredGameMap layeredGameMap;
    private final GridGameMap gameMap;
    private final int levelNumber;
    private MapNode bossNode;

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.gameMap = new GridGameMap();
        this.layeredGameMap = new LayeredGameMap();
        this.layeredGameMap.generateRandomMap(levelNumber);
    }

    public Level(int levelNumber, MapNode startNode) {
        this.levelNumber = levelNumber;
        this.gameMap = new GridGameMap();
        this.layeredGameMap = new LayeredGameMap();
        this.bossNode = gameMap.generateRandomMap(levelNumber, startNode);
    }


    public GridGameMap getGameMap() { return gameMap; }

    public LayeredGameMap getLayeredGameMap() { return layeredGameMap; }

    public int getLevelNumber() {
        return levelNumber;
    }

    public MapNode getBossNode() {
        return bossNode;
    }
}


