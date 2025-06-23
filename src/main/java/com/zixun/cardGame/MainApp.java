package com.zixun.cardGame;

import com.zixun.cardGame.util.CardLoader;
import com.zixun.cardGame.util.MonsterDataLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 初始化配置
        MonsterDataLoader.loadAll(
                "com/zixun/cardGame/config/monster.json");
        CardLoader.loadAll(
                "com/zixun/cardGame/config/card.json");

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(
                "/com/zixun/cardGame/view/main-view.fxml"));

//        Scene scene = new Scene(fxmlLoader.load(), 1280, 860);
        Scene scene = new Scene(fxmlLoader.load(), 1600, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(
                "/com/zixun/cardGame/view/style.fx.css")).toExternalForm());

        stage.setTitle("回合制卡牌游戏");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}