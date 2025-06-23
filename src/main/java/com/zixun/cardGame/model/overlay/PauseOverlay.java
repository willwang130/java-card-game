package com.zixun.cardGame.model.overlay;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PauseOverlay extends BaseOverlay {

    public PauseOverlay(Runnable onResume, Runnable onRestart, Runnable onMainMenu,Runnable onExit) {
        super(420, 260);
        getStyleClass().add("pause-overlay");

        VBox box = new VBox(18,
                makeButton("继续游戏", onResume),
                makeButton("重新开始", onRestart),
                makeButton("主菜单", onMainMenu),
                makeButton("退出游戏", onExit));
        box.setAlignment(Pos.CENTER);
        getChildren().add(box);
    }

    private Button makeButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        button.setMinWidth(160);
        return button;
    }
}
