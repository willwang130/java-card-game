package com.zixun.cardGame.map;

import com.almasb.fxgl.achievement.Achievement;
import com.almasb.fxgl.core.collection.grid.Cell;
import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.factory.MonsterFactory;
import com.zixun.cardGame.type.NodeContentEnum;
import com.zixun.cardGame.type.NodeTypeEnum;

import static com.zixun.cardGame.type.NodeContentEnum.*;

import java.util.*;
import java.util.stream.Collectors;

public class LayeredGameMap {
    public static final int CELL = GameController.CELL;
    public static final int GAP = GameController.GAP;
    public static final int PIXEL_X = CELL + GAP;
    public static final int PIXEL_Y = 120;
    public static final int DEPTH = 18;
    public static final int MIN_ROW = 3;
    public static final int MAX_ROW = 5;
    public static final int SLOT_PER_ROW = 13;   // 每行最多 0‑12 列
    private static final double MAX_LINK_LENGTH = 250.0;
    List<List<MapNode>> mapRows = new ArrayList<>(DEPTH);

    private MapNode startNode;
    private MapNode bossNode;
    private MapNode standingNode = null;
    private int levelNumber;

    private final Map<MapNode, Integer> uiColMap = new HashMap<>();

    private static final Random random = new Random();

    public void generateRandomMap(int levelNumber) {
        mapRows.clear();
        this.levelNumber = levelNumber;
        createRowsStructure();
        initUiLayoutAndConnections();
        tagStartAndBoss(levelNumber);
        ensureBossConnection();
        fillContent(levelNumber);
        standingNode = startNode;
    }

    private void createRowsStructure() {
        int startBranches = 3 + random.nextInt(3); // 3~5
        int[] widths = new int[DEPTH];

        widths[0] = 1;                      // 起点
        widths[1] = startBranches;          // 起点前的小怪行
        widths[DEPTH - 1] = 1;              // Boss
        widths[DEPTH - 2] = startBranches;  // Boss 前的休息行

        for (int i = 2; i < DEPTH - 2; i++) {
            int prev = widths[i - 1];
            int delta = random.nextInt(3) - 1;
            widths[i] = Math.max(MIN_ROW, Math.min(MAX_ROW, prev + delta));
        }

        for (int row = 0; row < DEPTH; row++) {
            List<MapNode> list = new ArrayList<>();
            for (int col = 0; col < widths[row]; col++) {
                list.add(new MapNode(col, row));
            }
            mapRows.add(list);
        }
        System.out.println("[" + levelNumber +"] widths: " + Arrays.toString(widths));
    }

    private void initUiLayoutAndConnections() {
        uiColMap.clear();
        final int MID = SLOT_PER_ROW / 2;
        Set<MapNode> needsAdjustment = new HashSet<>();
        uiColMap.put(mapRows.get(0).get(0), MID);

        for (int row = 1; row < DEPTH - 1; row++) {
            List<MapNode> prevRow = mapRows.get(row - 1);
            List<MapNode> thisRow = mapRows.get(row);
            boolean[] used = new boolean[SLOT_PER_ROW];
            double lockProb = (row % 2 == 0) ? 0.4 : 0.8; // 偶数行 40%，奇数行 80%

            for (MapNode p : prevRow) {
                Integer c = uiColMap.get(p);
                if (c != null && random.nextDouble() < lockProb) {
                    used[c] = true;       // 按概率要求子节点错开
                }
            }

            thisRow.sort(Comparator.comparingInt(n -> n.col));

            for (MapNode node : thisRow) {
                int col = findColNearParents(prevRow, used, 2);
                if (col == -1) { col = findAnyAvailableCol(used); }
                if (col == -1) { col = SLOT_PER_ROW / 2; }
                uiColMap.put(node, col);
                used[col] = true;

                // 尝试在 +-3 范围找最近的 parent 进行连接
                final int finalCol = col;
                final int finalRow = row;
                Optional<MapNode> optWithin3 = prevRow.stream()
                        .filter(p -> uiColMap.containsKey(p))
                        .filter(p -> getPixelDistance(uiColMap.get(p), finalCol, p.row, finalRow) <= MAX_LINK_LENGTH)
                        .min(Comparator.comparingDouble(p -> getPixelDistance(uiColMap.get(p), finalCol, p.row, finalRow)));

                MapNode parent = optWithin3.orElseGet(() ->
                        // 若没有找最近的parent
                        prevRow.stream()
                                .filter((p -> uiColMap.containsKey(p)))
                                .min(Comparator.comparingDouble(p -> getPixelDistance(uiColMap.get(p), finalCol, p.row, finalRow)))
                                .orElse(null));

                if (parent != null) {
                    if (printLongDistance(parent, node)) {
                        needsAdjustment.add(node);
                        continue;
                    }
                    parent.connectedEdges.add(node); // 单向 parent -> child
                    connectNodes(parent, node);
                } else {
                    System.out.println("Parent is NULL");
                }
            }

            // 补救：fallback 1: 确保每个 child 至少连上一个 parent
            for (MapNode child : thisRow) {
                boolean hasParent = prevRow.stream().anyMatch(p -> p.connectedEdges.contains(child));
                if (hasParent) continue;

                final int childCol = uiColMap.get(child);
                final int childRow = child.row;
                // 先尝试使用列差 ≤ 3 的 parent
                MapNode fallbackParent = prevRow.stream()
                        .filter(p -> uiColMap.containsKey(p))
                        .filter(p -> getPixelDistance(uiColMap.get(p), childCol, p.row, childRow) <= MAX_LINK_LENGTH)
                        .min(Comparator.comparingDouble(p -> getPixelDistance(uiColMap.get(p), childCol, p.row, childRow)))
                        .orElseGet(() ->  prevRow.stream()
                                .filter(p -> uiColMap.containsKey(p))
                                .min(Comparator.comparingDouble(p -> getPixelDistance(uiColMap.get(p), childCol, p.row, childRow)))
                                .orElse(null));

                if (fallbackParent != null && !fallbackParent.connectedEdges.contains(child)) {
                    fallbackParent.connectedEdges.add(child);
                    connectNodes(fallbackParent, child);
                    if (printLongDistance(fallbackParent, child)) {
                        needsAdjustment.add(child);
                    }
                }
            }

            // fallback 2：确保每个 parent 至少连一个 child
            for (MapNode parent : prevRow) {
                boolean hasChild = !parent.connectedEdges.isEmpty();
                if (hasChild) continue;
                final int parentCol = uiColMap.get(parent);
                final int parentRow = parent.row;
                // 先找列差 ≤3 的 child
                MapNode fallbackChild = thisRow.stream()
                        .filter(c -> getPixelDistance(uiColMap.get(c), parentCol, c.row, parentRow) <= MAX_LINK_LENGTH)
                        .min(Comparator.comparingDouble(c -> getPixelDistance(uiColMap.get(c), parentCol, c.row, parentRow)))
                        .orElseGet(() -> thisRow.stream()
                                .min(Comparator.comparingDouble(c -> getPixelDistance(uiColMap.get(c), parentCol, c.row, parentRow)))
                                .orElse(null));

                if (fallbackChild != null && !parent.connectedEdges.contains(fallbackChild)) {
                    parent.connectedEdges.add(fallbackChild);
                    connectNodes(parent, fallbackChild);
                    if (printLongDistance(parent, fallbackChild)) {
                        needsAdjustment.add(fallbackChild);
                    }
                }
            }

            // 所有 fallback 已完成，此时 needsAdjustment 已收集好
            for (MapNode node : thisRow) {
                if (!needsAdjustment.contains(node)) continue;

                // 查找所有连接到它的父节点（来自上一行）
                List<MapNode> connectedParents = prevRow.stream()
                        .filter(p -> p.connectedEdges.contains(node))
                        .toList();

                // 对这个 node 进行微调位置
                repositionNodeForShortestLinks(node, connectedParents, thisRow, used);
            }
        }
    }

    private int findColNearParents(List<MapNode> parents, boolean[] used, int limit) {
        List<Integer> cols = parents.stream().map(uiColMap::get).filter(Objects::nonNull).toList();
        List<Integer> candidates = new ArrayList<>();
        for (int col : cols) {
            for (int i = 0; i <= limit; i++) {
                if (col - i >= 0 && !used[col - i]) candidates.add(col - i);
                if (col + i < SLOT_PER_ROW && !used[col + i]) candidates.add(col + i);
            }
        }
        return candidates.isEmpty() ? -1 : candidates.get(random.nextInt(candidates.size()));
    }
    private void connectNodes(MapNode parent, MapNode child) {
        parent.children.add(child);
        child.parents.add(parent);
    }
    private int findAnyAvailableCol(boolean[] used) {
        List<Integer> candidate = new ArrayList<>();
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                candidate.add(i);
            }
        }
        return candidate.isEmpty() ? -1 : candidate.get(random.nextInt(candidate.size()));
    }

    private boolean repositionNodeForShortestLinks(
            MapNode node, List<MapNode> connected, List<MapNode> row, boolean[] used) {
        int selfCol = uiColMap.get(node);
        List<Integer> thisRowCols = row.stream().map(uiColMap::get).sorted().toList();
        int idx = thisRowCols.indexOf(selfCol);

        int colStart = (idx > 0) ? thisRowCols.get(idx - 1) + 1 : 0;
        int colEnd = (idx < thisRowCols.size() - 1) ? thisRowCols.get(idx + 1) - 1 : SLOT_PER_ROW - 1;

        int bestCol = selfCol;
        double minTotalDistance = Double.MAX_VALUE;

        for (int candidateCol = colStart; candidateCol <= colEnd; candidateCol++) {
            if (used[candidateCol] && candidateCol != selfCol) continue;

            double totalDistacne = 0.0;
            for (MapNode neighor : connected) {
                int colA = candidateCol;
                int colB = uiColMap.get(neighor);
                int rowA = node.row;
                int rowB = neighor.row;

                totalDistacne += getPixelDistance(colA, colB, rowA, rowB);
            }

            if (totalDistacne < minTotalDistance) {
                minTotalDistance = totalDistacne;
                bestCol = candidateCol;
            }
        }
        if (bestCol != selfCol) {
            uiColMap.put(node, bestCol);
            used[selfCol] = false;
            used[bestCol] = true;
            //System.out.println("[SmartMove] " + node + " moved from " + selfCol + " → " + bestCol + "  (dist=" + minTotalDistance + ")");
            return true;
        }
        //System.out.println("[SmartMove][Fail] " + node + " tried to reposition in (" + colStart + " ~ " + colEnd + ") but no better found");
        return false;
    }


    private double getPixelDistance(int colA, int colB, int rowA, int rowB) {
        double dx = Math.abs(colA - colB) * PIXEL_X;
        double dy = Math.abs(rowA - rowB) * PIXEL_Y;
        return Math.hypot(dx, dy);
    }

    private boolean printLongDistance(MapNode a, MapNode b) {
        int colA = uiColMap.get(a);
        int colB = uiColMap.get(b);
        int rowA = a.row;
        int rowB = b.row;

        double len = getPixelDistance(colA, colB, rowA, rowB);

        if (len > MAX_LINK_LENGTH) {
//            System.out.println("[level=" + levelNumber + "]" +
//                    "[" + a.content.getName() + "→" + b.content.getName() + "] " +
//                    "(" + rowA + "," + a.col + ")[UI col=" + colA + "] → " +
//                    "(" + rowB + "," + b.col + ")[UI col=" + colB + "] | pxLen=" + len);
            return true;
        }
        return false;
    }

    private void tagStartAndBoss(int levelNumber) {
        startNode = mapRows.get(0).get(0);
        startNode.content = START_EVENT;
        startNode.description = "一切的起点";

        for (MapNode node : mapRows.get(1)) {
//            node.type = NodeTypeEnum.START;
            node.content = ENEMY;
            node.monster = MonsterFactory.getRandomNormal(levelNumber);
        }

        MapNode boss = mapRows.get(DEPTH - 1).get(0);
        boss.content = FINAL_BOSS;
        boss.type = NodeTypeEnum.BOSS;
        boss.monster = MonsterFactory.getBoss(levelNumber);
        bossNode = boss;
        uiColMap.put(bossNode, SLOT_PER_ROW / 2);

        mapRows.get(DEPTH - 2).forEach(n -> { n.content = REST; });
    }

    private void ensureBossConnection() {
        List<MapNode> secondLastRow = mapRows.get(DEPTH - 2);
        secondLastRow.forEach(n -> {
                    n.connectedEdges.add(bossNode);
                    printLongDistance(n, bossNode);
                });

    }

    private void fillContent(int levelNumber) {

        // 1. 宝箱行（1~2 行）
        placeChestRows(1 + random.nextInt(2), 0.9, 4);

        // 2. 精英行（1 行 + 附加精英）
        placeEliteRow(1, 0.6, 6);

        placeExtraElites(5 + random.nextInt(3), 6);  // 附加精英4~6个，加连续4个最多1个的约束

        // 3. 商店 5~6个
        distribute(SHOP, 5 + random.nextInt(2), levelNumber, 3);

        // 4. 休息 5~6个
        distribute(REST, 5 + random.nextInt(2), levelNumber, 5);

        // 5. 剩余均匀分配 事件与小怪
        distributeEventAndEnemies(levelNumber);
    }
    private void placeChestRows(int rowCount, double fillRate, int minRowIndex) {
        List<List<MapNode>> candidates = new ArrayList<>(mapRows);
        Collections.shuffle(candidates);

        for (List<MapNode> row : candidates) {
            if (row.isEmpty()) continue;
            MapNode first = row.get(0);
            if (first == null || first.row < minRowIndex || first.content != NONE) continue;
            for (MapNode node : row) {
                if (node.content == NONE && Math.random() < fillRate) {
                    node.content = TREASURE;
                }
            }
            if (--rowCount == 0) break;
        }
    }

    private void placeEliteRow(int rowCount, double fillRate, int minRowIndex) {
        List<List<MapNode>> candidates = new ArrayList<>(mapRows);
        Collections.shuffle(candidates);

        for (List<MapNode> row : candidates) {

            MapNode first = row.get(0);
            if (first == null || first.row < minRowIndex || first.content != NONE) continue;

            for (MapNode node : row) {
                if (node.content == NONE && Math.random() < fillRate) {
                    node.content = MINI_BOSS;
                    node.monster = MonsterFactory.getRandomElite(levelNumber);
                }
            }
            if (--rowCount == 0) break;
        }
    }

    private void placeExtraElites(int count, int minRowIndex) {
        List<MapNode> pool = getPool();
        Collections.shuffle(pool);
        int placed = 0;

        for (MapNode node : pool) {
            if (node.row < minRowIndex) continue;
            if (hasNearbySameContent(node)) continue;
            if (node.content == NONE) {
                node.content = MINI_BOSS;
                node.monster = MonsterFactory.getRandomElite(levelNumber);
            }
            if (++placed >= count) break;
        }
    }

    private boolean hasNearbySameContent(MapNode node) {
        for (MapNode parent : node.parents) {
            if (parent.content != NONE && node.content != NONE
                    && parent.content == node.content) return true;
        }
        for (MapNode child : node.children) {
            if (child.content != NONE && node.content != NONE
                    && child.content == node.content) return true;
        }
        return false;
    }

    private void distribute(NodeContentEnum contentEnum, int count, int levelNumber, int minRowIndex) {
        List<MapNode> pool = getPool();
        Collections.shuffle(pool);
        for (MapNode node : pool) {
            if (node.row < minRowIndex) continue;
            if (hasNearbySameContent(node)) continue;
            if (node.content == NONE) {
                node.content = contentEnum;
                switch (contentEnum) {
                    case ENEMY -> node.monster = MonsterFactory.getRandomNormal(levelNumber);
                    case MINI_BOSS -> node.monster = MonsterFactory.getRandomElite(levelNumber);
                }
                if (--count == 0) break;
            }
        }
    }

    private void distributeEventAndEnemies(int levelNumber) {
        List<MapNode> pool = getPool();
        Collections.shuffle(pool);
        int half = pool.size() / 2;
        int index = 0;

        for (; index < half; index++) {
            MapNode node = pool.get(index);
            node.content = EVENT;
        }

        for (; index < pool.size(); index++) {
            MapNode node = pool.get(index);
            node.content = ENEMY;
            node.monster = MonsterFactory.getRandomNormal(levelNumber);
        }
    }

    private List<MapNode> getPool() {
        return mapRows.stream()
                .flatMap(List::stream)
                .filter(n -> n.content == NONE)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public MapNode getStartNode() { return startNode; }
    public List<MapNode> getAdjacentNodes(MapNode node) {
        List<MapNode> list = new ArrayList<>();
        for (MapNode n : getMapRows().get(node.row + 1)) {
            if (canMove(node, n)) list.add(n);
        }
        return list;
    }

    public boolean canMove(MapNode from, MapNode to) {
        if (from == null) return to.row == 0;
        if (to.row != from.row + 1) return false;
        return from.connectedEdges.contains(to);
    }

    public void moveTo(MapNode to) { standingNode = to; }
    public List<List<MapNode>> getMapRows() { return mapRows; }
    public MapNode getPlayerNode() { return standingNode;  }
    public MapNode getBossNode() { return bossNode; }
    public void resetPlayerToStart() { standingNode = null; }
    public int getUiCol(MapNode node) { return uiColMap.get(node); }
    public boolean hasUiLayout() { return !uiColMap.isEmpty(); }

    private boolean allReachable() {
        Set<MapNode> visited = new HashSet<>();
        Deque<MapNode> q = new ArrayDeque<>();
        q.add(startNode); visited.add(startNode);

        while (!q.isEmpty()) {
            MapNode cur = q.poll();
            for (MapNode nxt : cur.connectedEdges) {
                if (visited.add(nxt)) q.add(nxt);
            }
        }
        long total = mapRows.stream().flatMap(List::stream).count();
        return visited.size() == total;
    }
}
