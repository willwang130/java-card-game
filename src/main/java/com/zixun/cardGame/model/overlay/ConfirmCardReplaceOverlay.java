package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.util.PriceCalculator;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.control.ScrollPane;

public class ConfirmCardReplaceOverlay extends BaseOverlay {
    public ConfirmCardReplaceOverlay(Card newCard, GameController controller, Runnable onDone) {
        super(500, 400);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_CENTER);

        Label info = new Label("你的卡组已满. \n请选择一张卡牌进行替换: ");
        info.setStyle("-fx-font-size: 16px;");

        Label newCardLabel = new Label(
                "新卡牌: " + CardView.displayName(newCard) + " - "
                        + CardView.displayName(newCard) + " - 金币:" + PriceCalculator.getRandomPriceForCard(newCard, false, false));
        newCardLabel.setWrapText(true);
        newCardLabel.setStyle("-fx-text-fill: darkgreen");

        TilePane cardPane = new TilePane();
        cardPane.setPrefColumns(3);
        cardPane.setHgap(10);
        cardPane.setVgap(10);
        cardPane.setPadding(new Insets(5));

        for (Card oldCard : controller.getDeckManager().getAllCards()) {
            Button button = new Button(oldCard.getName() + "\n" + oldCard.getDescription());
            button.setPrefSize(160, 80);
            button.setOnAction(e -> {

                // 顺序添加
                Map<String, Runnable> buttonMap = new LinkedHashMap<>();
                buttonMap.put("确认", () -> {
                    controller.getDeckManager().removeCard(oldCard);
                            controller.getDeckManager().addCard(newCard);
                            closeOverlay();
                            onDone.run();
                });
                buttonMap.put("取消", () -> {});
                // 2次确认
                OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                        new InfoOverlay("替换确认",
                                "是否将 [" + oldCard.getName() + "] 替换为 [" + newCard.getName() + "] ?",
                                buttonMap
                        )
                );
            });
            cardPane.getChildren().add(button);
        }

        Button cancelBtn = new Button("返回卡牌选择");
        cancelBtn.setOnAction(e -> closeOverlay());

        ScrollPane scrollPane = new ScrollPane(cardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent;");

        box.getChildren().addAll(info, newCardLabel, scrollPane, cancelBtn);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}