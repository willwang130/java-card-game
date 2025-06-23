package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.model.relic.Relic;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class RelicDetailOverlay extends BaseOverlay{

    public RelicDetailOverlay(Relic relic) {
        super(400, 300);

        Label nameLabel = new Label("宝物名称: " + relic.getName());
        Label descLabel = new Label("效果:\n" + relic.getDescription());

        nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: black;");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.LEFT);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        Button closeButton = new Button("关闭");
        closeButton.setOnAction(e -> closeOverlay());

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        box.getChildren().addAll(nameLabel, descLabel, closeButton);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
