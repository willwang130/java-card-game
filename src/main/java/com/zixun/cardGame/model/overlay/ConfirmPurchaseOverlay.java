package com.zixun.cardGame.model.overlay;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.control.Label;

public class ConfirmPurchaseOverlay extends BaseOverlay{
    public ConfirmPurchaseOverlay(String name, String description, int price, Runnable onConfirm) {
        super(420, 260);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(name);
        Label descLabel = new Label(description);
        Label priceLabel = new Label("价格: " + price + " 金币");

        Button confirmButton = new Button("购买");
        Button cancelButton = new Button("取消");

        confirmButton.setOnAction(e -> {
            closeOverlay();
            if (onConfirm != null) onConfirm.run();
        });
        cancelButton.setOnAction(e -> closeOverlay());

        HBox buttonBox = new HBox(20, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(nameLabel, descLabel, priceLabel, buttonBox);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
