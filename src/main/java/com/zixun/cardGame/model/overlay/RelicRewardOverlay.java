package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.util.OverlayUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class RelicRewardOverlay extends BaseOverlay {
    public RelicRewardOverlay(String titleText, String subtitleText,
                              List<Relic> rewardRelics, GameController controller,
                              Runnable onDone) {
        super(500, 300);
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        HBox cardButtons = new HBox(20);
        cardButtons.setAlignment(Pos.CENTER);

        for (Relic relic : rewardRelics) {
            Button button = new Button(
                    relic.getName() + "\n" + relic.getDescription());
            button.setPrefSize(150, 100);
            button.setWrapText(true);
            button.setTextAlignment(TextAlignment.CENTER);
            button.setOnAction(e -> {
                if (controller.getPlayer().getRelicList().contains(relic)) {
                    // Relic 已拥有
                    OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                            new ConfirmOverlay(
                                    "已拥有该宝物, 是否直接兑换" + relic.getPrice() + "金币?",
                                    () -> {
                                        controller.getPlayer().addGold(relic.getPrice());
                                        closeOverlay();
                                        onDone.run();
                                    }
                            )
                    );
                } else {
                    controller.getPlayer().addAndEquipRelic(relic);
                    closeOverlay();
                    onDone.run();
                }
            });
            cardButtons.getChildren().add(button);
        };

        box.getChildren().addAll(title, cardButtons);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
