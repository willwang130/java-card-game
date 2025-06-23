package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class CardDetailOverlay extends BaseOverlay{


    public CardDetailOverlay(Card card, Player player) {
        super(400, 250);

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label nameAndCostLabel = new Label("EP:" + card.getCost() + " " + CardView.displayName(card));
        String rarityColor = switch (card.getRarity()) {
            case "common" -> "-fx-background-color: lightgray;";
            case "uncommon" -> "-fx-background-color: lightblue;";
            case "rare" -> "-fx-background-color: lightyellow;";
            default -> "-fx-background-color: red;";
        };
        Label typeLabel = new Label(card.getType());
        Label descLabel = new Label(CardView.createDynamicDescriptionString(card, player));

        box.setStyle(rarityColor);
        nameAndCostLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: black;");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.LEFT);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        Button closeButton = new Button("关闭");
        closeButton.setOnAction(e -> closeOverlay());

        box.getChildren().addAll(nameAndCostLabel, typeLabel, descLabel, closeButton);
        this.getChildren().add(box);
        this.setAlignment(Pos.CENTER);
    }
}
