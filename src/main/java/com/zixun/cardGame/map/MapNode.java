package com.zixun.cardGame.map;

import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.type.NodeContentEnum;
import com.zixun.cardGame.type.NodeTypeEnum;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MapNode {
    public int row;     // 行深 0 ~ 16
    public int col;     // 行内序号
    public int regionId = -1;
    public boolean visited = false;
    public Set<MapNode> connectedEdges = new HashSet<>();
    public Set<Integer> connectedRegions = new HashSet<>();


    public Set<MapNode> parents = new HashSet<>();
    public Set<MapNode> children = new HashSet<>();

    public NodeTypeEnum type = NodeTypeEnum.EMPTY;
    public NodeContentEnum content = NodeContentEnum.NONE;
    public Monster monster;
    public String description = "";

    public MapNode(int col, int row) {
        this.col = col;
        this.row = row;
    };

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean b) {
        visited = b;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        MapNode other = (MapNode) obj;
//        return this.x == other.x && this.y == other.y;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(x, y);
//    }
}
