package com.zixun.cardGame.model.overlay;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConfirmOverlay extends BaseOverlay{
    public ConfirmOverlay(String titleText, Runnable onConfirm) {
        super(420, 260);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label title = new Label(titleText);

        Button confirmButton = new Button("确定");
        Button cancelButton = new Button("取消");

        confirmButton.setOnAction(e -> {
            closeOverlay();
            if (onConfirm != null) onConfirm.run();
        });
        cancelButton.setOnAction(e -> closeOverlay());

        HBox buttonBox = new HBox(20, confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(title, buttonBox);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
