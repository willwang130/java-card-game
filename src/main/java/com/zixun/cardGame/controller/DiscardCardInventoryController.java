package com.zixun.cardGame.controller;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.model.overlay.CardDetailOverlay;
import com.zixun.cardGame.model.overlay.ConfirmCardDiscardOverlay;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.util.PopupHelper;
import com.zixun.cardGame.util.PriceCalculator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class DiscardCardInventoryController {
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TilePane cardPane;
    @FXML private Label remainingDiscardLabel;

    private List<Card> cards;
    private Consumer<Card> onCardClicked;
    private Runnable onRefresh;
    private Player player;
    private boolean discardMode = false;
    private int discardChanceLeft = 0;

    private Runnable onCloseRequest;
    private StackPane rootPane;
    private Deque<Node> overlayStack;


    public void initialize(
            Player player, List<Card> cards, StackPane rootPane, Deque<Node> overlayStack) {
        this.player = player;
        this.cards = new ArrayList<>(cards);
        this.rootPane = rootPane;
        this.overlayStack = overlayStack;
        sortComboBox.getItems().addAll("名称", "类型", "等级");
        sortComboBox.setValue("类型");
        
        sortComboBox.setOnAction(e -> renderCards());

        renderCards();
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
            Button button = new Button(card.getName() + "\nLv" + card.getLevel());
            button.setPrefSize(100, 60);
            PopupHelper.bind(button, card.getDescription());

            button.setOnAction(e -> {
                if (discardMode) {
                    if (discardChanceLeft <= 0) {
                        OverlayUtil.showInfo(rootPane, overlayStack, """
                                已达到本层弃牌上限!\s
                                请进入下一层继续探索.\s
                                
                                点击关闭继续浏览卡牌.\s""");
                    } else {
                        showCardDiscardDetail(card);
                    }
                } else {
                    showCardDetail(card);
                }
            });
            cardPane.getChildren().add(button);
        }
    }

    public void showCardDetail(Card card) {
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> new CardDetailOverlay(card, player));
    }

    public void showCardDiscardDetail(Card card) {
        OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                new ConfirmCardDiscardOverlay(card, player, () -> {
                    if (onCardClicked != null) {
                        onCardClicked.accept(card);
                        player.addGold(-PriceCalculator.getRandomPriceForCard(card, false, false));
                        if (onRefresh != null) {
                            onRefresh.run();
                        }
                    }
                })
        );
    }

    public void refresh(List<Card> updatedCards) {
        this.cards = new ArrayList<>(updatedCards);
        renderCards();
    }

    @FXML
    private void onClose() {
        if (onCloseRequest != null) {
            onCloseRequest.run();
        }
    }

    public Runnable getRenderHand() {
        return this::renderCards;
    }
    
    public void setOnCloseRequest(Runnable onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    public void setOnCardClicked(Consumer<Card> handler) {
        this.onCardClicked = handler;
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    public void setDiscardMode(boolean enabled) {
        this.discardMode = enabled;
    }

    public void setDiscardChanceLeft(int chance) {
        this.discardChanceLeft = chance;
    }

    public void setDiscardChanceLeftLabel(int chance) {
        if (remainingDiscardLabel != null) {
            remainingDiscardLabel.setText("你还可以弃牌 " + chance + " 次");
        }
    }
}
