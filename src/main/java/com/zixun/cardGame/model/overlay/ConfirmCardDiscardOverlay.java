package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConfirmCardDiscardOverlay extends BaseOverlay{
    public ConfirmCardDiscardOverlay(Card card, Player player, Runnable onConfirm) {
        super(450, 280);

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(20));
        Label nameLabel = new Label(CardView.displayName(card));
        Label valueLabel = new Label(card.getRarity());
        Label typeLabel = new Label(card.getType());
        Label descLabel = new Label("\n" + CardView.createDynamicDescriptionString(card, player));
        Label confirmLabel = new Label("是否确认丢弃该卡牌？");

        nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: black;");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        valueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        descLabel.setWrapText(true);
        confirmLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 14px;");

        Button confirmBtn = new Button("丢弃");
        Button cancelBtn = new Button("关闭");

        confirmBtn.setOnAction(e -> {
            closeOverlay();
            if (onConfirm != null) onConfirm.run();
        });

        cancelBtn.setOnAction(e -> closeOverlay());

        HBox btnBox = new HBox(20, confirmBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(nameLabel, typeLabel, valueLabel, descLabel, confirmLabel, btnBox);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}