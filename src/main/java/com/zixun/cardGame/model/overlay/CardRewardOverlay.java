package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class CardRewardOverlay extends BaseOverlay {
    public CardRewardOverlay(String titleText, String subtitleText,
                             List<Card> rewardCards, GameController controller,
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

        for (Card card : rewardCards) {
            Button button = new Button(
                    CardView.displayName(card) + CardView.createDynamicDescriptionString(card, controller.getPlayer()));
            button.setPrefSize(250, 200);
            button.setWrapText(true);
            button.setTextAlignment(TextAlignment.CENTER);
            button.setOnAction(e -> {
                if (controller.getDeckManager().getAllCards().size() >= DeckManager.MAX_DECK) {
                    // 卡牌已满 弹出替换窗口
                    OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                            new ConfirmCardReplaceOverlay(card, controller, () -> {
                                closeOverlay();
                                onDone.run();
                            })
                    );
                } else {
                    controller.getDeckManager().addCard(card);
                    closeOverlay();
                    onDone.run();
                }
            });
            cardButtons.getChildren().add(button);
        };
        Button giveUpBtn = new Button("返回地图");
        //Button giveUpBtn = new Button("放弃奖励 (获得最贵卡牌的金币)");
        giveUpBtn.setOnAction(e -> {
//            int maxGold = rewardCards.stream().mapToInt(Card::getPrice).max().orElse(0);
//            controller.getPlayer().addGold(maxGold);
//            OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
//                    new InfoOverlay("你获得了 " + maxGold + " 金币", onDone)
//            );
            closeOverlay();
            onDone.run();
        });

        box.getChildren().addAll(title, cardButtons, giveUpBtn);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
