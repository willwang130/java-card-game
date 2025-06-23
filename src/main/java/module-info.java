module org.example.rdbgameproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;
    requires java.smartcardio;
    requires jdk.compiler;
    requires com.fasterxml.jackson.databind;
    requires annotations;
    requires java.desktop;

    exports com.zixun.cardGame.controller;
    opens com.zixun.cardGame.controller to javafx.fxml;

    exports com.zixun.cardGame.model.character;
    exports com.zixun.cardGame.model.card;
    exports com.zixun.cardGame.type;
    exports com.zixun.cardGame.manager;
    opens com.zixun.cardGame.manager to javafx.fxml;
    exports com.zixun.cardGame.behavior;
    opens com.zixun.cardGame.behavior to javafx.fxml;
    exports com.zixun.cardGame;
    opens com.zixun.cardGame to javafx.fxml;
    exports com.zixun.cardGame.map;
    opens com.zixun.cardGame.map to javafx.fxml;
    exports com.zixun.cardGame.util;
    opens com.zixun.cardGame.util to javafx.fxml;

    exports com.zixun.cardGame.model.relic;
    exports com.zixun.cardGame.behavior.strategy;
    opens com.zixun.cardGame.behavior.strategy to javafx.fxml;
    exports com.zixun.cardGame.animation;
    opens com.zixun.cardGame.animation to javafx.fxml;
}