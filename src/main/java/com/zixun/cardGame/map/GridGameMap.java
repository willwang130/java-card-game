package com.zixun.cardGame.map;

import com.zixun.cardGame.factory.MonsterFactory;
import com.zixun.cardGame.model.character.Monster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.zixun.cardGame.type.NodeContentEnum.*;
import static com.zixun.cardGame.type.NodeTypeEnum.*;

public class GridGameMap {
    private static final int DEPTH = 5;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    public static final int BRANCH_MIN_LENGTH = 2;
    public static final int BRANCH_MAX_LENGTH = 8;
    private static final Random random = new Random();

    private final MapNode[][] grid = new MapNode[WIDTH][HEIGHT];
    private final List<MapNode> mainPath = new ArrayList<>();
    private final List<List<MapNode>> branches = new ArrayList<>();

    private MapNode startNode;
    private MapNode bossNode;
    private MapNode standingNode;

    public MapNode generateRandomMap(int levelNumber, MapNode preStartNode) {
        // 1. 初始化空地图
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                grid[x][y] = new MapNode(x, y);
            }
        }
        // 2. 确认起始点
        if (preStartNode == null) {
            startNode = grid[0][HEIGHT - 1];
        } else {
            startNode = grid[preStartNode.col][preStartNode.row];
        }
        startNode.type = START;
        standingNode = startNode;

        // 3. 随机生成 BOSS 点
        int attempts = 0;
        do {
            bossNode = generateNode();
            if (attempts++ > 100) {
                throw new RuntimeException("Boss 生成失败，可能空间不够");
            }
        } while (distanceTo(startNode, bossNode) < 5 || bossNode == startNode);

        bossNode.type = BOSS;
        bossNode.content = FINAL_BOSS;
        bossNode.monster = setBoss(levelNumber);
        System.out.println("FirstTime SetBossNode\n");

        // 4. 生成主路径
        buildMainPath(startNode, bossNode);

        // 5. 从主路经生成随机分支路径
        buildBranches();

        // 6. 放置分支事件 (宝箱/小boss)
        decorateBranches();

        // 6. 放置全局事件 (ENEMY/REST)
        decorateAllGrid();

        // 7. 随机分支之间互相连接
        connectBranchesRandomly();

        // 8. 设置一个商店
        setOneShop();

        return bossNode;
    }

    private MapNode generateNode() {
        int x = random.nextInt(WIDTH);
        int y = random.nextInt(HEIGHT);
        return grid[x][y];
    }

    private Monster setBoss(int levelNumber) {
        return MonsterFactory.getBoss(levelNumber);
    }

    private void buildMainPath(MapNode startNode, MapNode bossNode) {
        MapNode currentNode = startNode;
        mainPath.add(currentNode);

        while (!isNeighbor(currentNode, bossNode)) {
            List<MapNode> candidates = getEmptyNeighbors(currentNode);
            // 无路可走(小概率)
            if (candidates.isEmpty()) break;
            // 距离BOSS最近的格子排序
            candidates.sort(Comparator.comparingInt(a -> distanceTo(a, bossNode)));
            currentNode = candidates.get(0);
            currentNode.type = MAIN_PATH;
            mainPath.add(currentNode);
            System.out.println("Set current type x:" + currentNode.row + " y:" + currentNode.col + "\n");
        }
        // 最后连接BOSS节点
        bossNode.type = BOSS;
        bossNode.content = FINAL_BOSS;
        mainPath.add(bossNode);

        // 空白格:-1 主路经：0 分支：1, 2, 3...
        for (MapNode node : mainPath) {
            node.regionId = 0;
        }
    }

    private boolean isNeighbor(MapNode currentNode, MapNode bossNode) {
        return distanceTo(currentNode, bossNode) == 1;
    }

    private int distanceTo(MapNode a, MapNode b) {
        // 曼哈顿距离
        return Math.abs(a.col - b.col) + Math.abs(a.row - b.row);
    }

    private List<MapNode> getEmptyNeighbors(MapNode currentNode) {
        List<MapNode> neighbors = new ArrayList<>();
        int x = currentNode.col;
        int y = currentNode.row;

        // 左边是否有
        if (x > 0 && isClean(grid[x - 1][y])) neighbors.add(grid[x - 1][y]);
        // 右边是否有
        if (x < WIDTH - 1 && isClean(grid[x + 1][y])) neighbors.add(grid[x + 1][y]);
        // 上面是否有
        if (y > 0 && isClean(grid[x][y - 1])) neighbors.add(grid[x][y - 1]);
        // 下面是否有
        if (y < HEIGHT - 1 && isClean(grid[x][y + 1])) neighbors.add(grid[x][y + 1]);

        return neighbors;
    }

    private MapNode findEmptyNeighbor(MapNode node) {
        List<MapNode> empty = getEmptyNeighbors(node);
        if (empty.isEmpty()) return null;
        return empty.get(random.nextInt(empty.size()));
    }

    private boolean isClean(MapNode node) {
        return node.type == EMPTY && node.content == NONE;
    }

    private void buildBranches() {
        int nextRegionId = 1;

        for (MapNode mainNode : mainPath) {
            if (mainNode == bossNode) continue;
            // 35% 生成分支
            if (random.nextDouble() < 0.35) {
                MapNode branchStart = findEmptyNeighbor(mainNode);
                if (branchStart == null) continue;

                List<MapNode> branch = new ArrayList<>();
                int branchLength = BRANCH_MIN_LENGTH + random.nextInt(BRANCH_MAX_LENGTH); // length min ~ max

                branch.add(branchStart);
                branchStart.type = BRANCH_PATH;
                branchStart.regionId = nextRegionId;

                // 设置连接关系
                mainNode.connectedRegions.add(nextRegionId);
                branchStart.connectedRegions.add(0);

                MapNode current = branchStart;

                for (int i = 1; i < branchLength; i++) {
                    List<MapNode> candidates = getEmptyNeighbors(current);
                    if (candidates.isEmpty()) break;

                    MapNode next = candidates.get(random.nextInt(candidates.size()));
                    next.type = BRANCH_PATH;
                    next.regionId = nextRegionId;
                    branch.add(next);
                    current = next;
                }
                branches.add(branch);
                nextRegionId++;
            }
        }
    }

    private void connectBranchesRandomly() {
        for (int i = 0; i < branches.size(); i++) {
            for (int j = 0; j < branches.size(); j++) {
                List<MapNode> branchA = branches.get(i);
                List<MapNode> branchB = branches.get(j);

                for (MapNode nodeA : branchA) {
                    for (MapNode nodeB : branchB) {
                        if (isNeighbor(nodeA, nodeB)) {
                            if (random.nextDouble() < 0.2) {
                                nodeA.connectedRegions.add(nodeB.regionId);
                                nodeB.connectedRegions.add(nodeA.regionId);
                                System.out.println("连接分支 " + i + " 和 " + j);
                            }
                        }
                    }
                }
            }
        }
    }

    private void decorateAllGrid() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                MapNode node = grid[x][y];
                if ((node.type == MAIN_PATH || node.type == BRANCH_PATH) && node.content == NONE ) {
                    if (random.nextDouble() < 0.3) { // 30% 概率
                        randomContent(node);
                    }
                }
            }
        }
    }

    private void setOneShop() {
        boolean setOneShop = false;
        int attamps = 0;
        do {
            MapNode node = generateNode();
            if ((node.type == MAIN_PATH || node.type == BRANCH_PATH) && node.content == NONE ) {
                node.content = SHOP;
                setOneShop = true;
            } else if (attamps++ > 50) {
                System.out.println("商店未设置");
                break;
            }
        } while (!setOneShop);
    }

    private void decorateBranches() {
        for (List<MapNode> branch : branches) {
            MapNode randomMid = branch.get(random.nextInt(branch.size()));

            if (branch.size() < mainPath.size() / 2) {
                randomMid.content = MINI_BOSS; //小boss
                randomMid.monster = MonsterFactory.getRandomElite(1);
            } else {
                randomMid.content = random.nextBoolean() ? TREASURE : REST; // 奖励点
            }
        }
    }

    private void randomContent(MapNode node) {
        int r = random.nextInt(100);
        if (r < 50) {
             node.content = ENEMY;
             node.monster = MonsterFactory.getRandomNormal(1);
        } else if (r < 80) {
            node.content = EVENT;
        } else {
            node.content = random.nextBoolean() ? TREASURE : REST;
        }
    }

    public boolean canMove(MapNode from, MapNode to) {
        if (!isNeighbor(from, to)) return false;

        if (from.regionId == -1 || to.regionId == -1) return false;

        if (from.regionId == to.regionId) return true;

        return from.connectedRegions.contains(to.regionId)
                || to.connectedRegions.contains(from.regionId);
    }

    public MapNode getNode(int x, int y) {
        return grid[x][y];
    }

    public MapNode getPlayerNode() {
        return standingNode;
    }

    public void moveTo(int x, int y) {
        standingNode = grid[x][y];
    }

    public void resetPlayerToStart() {
        standingNode = startNode;
    }

    public MapNode getBossNode() {
        return bossNode;
    }
}
