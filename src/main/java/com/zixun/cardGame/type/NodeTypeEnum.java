package com.zixun.cardGame.type;

public enum NodeTypeEnum {
    EMPTY("空"),
    START("起点"),
    BOSS("BOSS"),
    MAIN_PATH("主路经"),
    BRANCH_PATH("分支路径");

    private final String name;

    NodeTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
