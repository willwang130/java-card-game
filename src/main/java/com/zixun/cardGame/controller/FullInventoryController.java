package com.zixun.cardGame.controller;

import com.zixun.cardGame.factory.RelicFactory;
import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.model.overlay.CardDetailOverlay;
import com.zixun.cardGame.model.overlay.RelicDetailOverlay;
import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.util.PopupHelper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

public class FullInventoryController {

    @FXML private ComboBox<String> sortComboBox;
    @FXML private TilePane cardPane;
    @FXML private TilePane relicPane;
    @FXML private Button closeButton;
    @FXML private Label backpackCardHold;
    @FXML private Label backpackGold;
    @FXML private Label relicsInTotal;

    private StackPane rootPane;
    private DeckManager deckManager;

    private Player player;
    private List<Card> cards;
    private List<Relic> relics;

    private Runnable onCloseRequest;
    private Deque<Node> overlayStack;


    public void initialize(Player player, List<Card> cards, List<Relic> relics, StackPane rootPane,
                           Deque<Node> overlayStack) {
        this.player = player;
        this.cards = new ArrayList<>(cards);
        this.relics = new ArrayList<>(relics);
        this.rootPane = rootPane;
        this.overlayStack = overlayStack;

        sortComboBox.getItems().addAll("名称", "类型", "等级");
        sortComboBox.setValue("类型");

        sortComboBox.setOnAction(e -> renderCards());

        renderCards();
        renderRelics();
    }

    private void renderCards() {
        Comparator<Card> comparator = switch (sortComboBox.getValue()) {
            case "类型" -> Comparator.comparing(Card::getType);
            case "等级" -> Comparator.comparingInt(Card::getLevel);
            default -> Comparator.comparing(Card::getName);
        };
        List<Card> sorted = cards.stream().sorted(comparator).toList();

        cardPane.getChildren().clear();
        for (Card card : sorted) {
            Button button = getButton(card);

            cardPane.getChildren().add(button);
        }

        updatePlayerInfo();
    }

    private Button getButton(Card card) {
        String rarityColor = switch (card.getRarity()) {
            case "common" -> "-fx-background-color: lightgray;";
            case "uncommon" -> "-fx-background-color: lightblue;";
            case "rare" -> "-fx-background-color: lightyellow;";
            default -> "-fx-background-color: red;";
        };

        String level = card.getLevel() == 0 ? "" : "+";
        Button button = new Button(card.getName() + level);
        button.setStyle(rarityColor);
        button.setPrefSize(100, 60);
        PopupHelper.bind(button, card.getDescription());

        button.setOnAction(e -> {
            OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                    new CardDetailOverlay(card ,player)
            );
        });
        return button;
    }

    private void updatePlayerInfo() {
        backpackGold.setText("金币: " + player.getGold());
        backpackCardHold.setText("卡牌持有: " + cards.size() + " 持有上限: " + DeckManager.MAX_DECK);
    }

    private void renderRelics() {
        relicPane.getChildren().clear();
        for (Relic relic : relics) {
            Label label = new Label(relic.getName());
            label.setStyle("-fx-background-color: lightgoldenrodyellow; -fx-padding: 5 10 5 10; -fx-border-color: darkgoldenrod;");
            // PopUp
            PopupHelper.bind(label, relic.getDescription());

            label.setOnMouseClicked(e -> {
                OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                        new RelicDetailOverlay(relic)
                );
            });

            relicPane.getChildren().add(label);
        }
        relicsInTotal.setText("宝物: " + relics.size() + " / " + RelicFactory.RELIC_IN_TOTAL);
    }

    @FXML
    private void onClose() {
        if (onCloseRequest != null) {
            onCloseRequest.run();
        }
    }

    public void setOnCloseRequest(Runnable onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    public Button getCloseButton() {
        return closeButton;
    }

}
