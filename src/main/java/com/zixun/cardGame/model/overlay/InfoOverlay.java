package com.zixun.cardGame.model.overlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.Map;


public class InfoOverlay extends BaseOverlay{

    public InfoOverlay(String message, Runnable onConfirm) {
        this("提示", message, Map.of("确定", onConfirm));
    }

    public InfoOverlay(String title, String message, Map<String, Runnable> options) {
        super(350, 200);

        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: black; -fx-font-weight: bold;");

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(TextAlignment.CENTER);
        msgLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        for (Map.Entry<String, Runnable> entry : options.entrySet()) {
            Button button = new Button(entry.getKey());
            button.setOnAction(e -> {
                closeOverlay();
                if (entry.getValue() != null) entry.getValue().run();
            });
            buttonBox.getChildren().add(button);
        }

        box.getChildren().addAll(titleLabel, msgLabel, buttonBox);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
