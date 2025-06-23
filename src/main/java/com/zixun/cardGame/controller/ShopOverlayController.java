package com.zixun.cardGame.controller;
import com.zixun.cardGame.model.overlay.BaseOverlay;
import com.zixun.cardGame.factory.RelicFactory;
import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.model.overlay.ConfirmPurchaseOverlay;
import com.zixun.cardGame.model.overlay.InfoOverlay;
import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.util.CardLoader;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.util.PriceCalculator;
import com.zixun.cardGame.util.PopupHelper;
import com.zixun.cardGame.view.CardView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;


import java.io.IOException;
import java.util.*;

public class ShopOverlayController {
    @FXML private GridPane shopGrid;
    @FXML private Label goldLabel;
    @FXML private Label shopHP;
    @FXML private Button   closeButton;
    @FXML private Button removeCardButton;

    private Player player;
    private DeckManager deckManager;
    private static Map<String, Card> allCards;
    private static List<Relic> allRelics;
    private List<Card> shopCardCache;
    private List<Relic> shopRelicCache;
    private boolean isShopActive;
    private boolean hasRested;
    private int remainingDiscardLeft;
    String discountedCardName;
    private final Map<Card, Integer> cardPriceMap = new HashMap<>();
    private final Map<Relic, Integer> relicPriceMap = new HashMap<>();

    private final int NUMBER_SHOW_CARDS = 8;
    private final int NUMBER_SHOW_RELIC = 3;

    private Runnable onUpdateUI;
    private Runnable onCloseRequest;
    private StackPane rootPane;
    private Deque<Node> overlayStack;


    static {
        allCards = new HashMap<>(CardLoader.getALLCards());
        allRelics = RelicFactory.getAllRelic();
    }

    public void initData(Player player, DeckManager deckManager, StackPane rootPane, Deque<Node> overlayStack) {
        this.player = player;
        this.deckManager = deckManager;
        this.rootPane = rootPane;
        this.overlayStack = overlayStack;

        closeButton.setFocusTraversable(false);

        // 随机刷新卡牌， Relic
        shopCardCache = generateRandomCards();
        shopRelicCache = generateRandomRelic();

        // 有随机一张打折
        int index = new Random().nextInt(shopCardCache.size());
        discountedCardName = shopCardCache.get(index).getName();

        // 缓存卡牌价格
        for (Card card : shopCardCache) {
            boolean isDiscounted = card.getName().equals(discountedCardName);
            cardPriceMap.put(card, PriceCalculator.getRandomPriceForCard(card, isDiscounted, player.isHalfPrice()));
        }
        // 缓存宝物价格
        for (Relic relic : shopRelicCache) {
            relicPriceMap.put(relic, PriceCalculator.getRandomPriceForRelic(player.isHalfPrice()));
        }

        // 剩余卡牌
        remainingDiscardLeft = 3;

        closeButton.setFocusTraversable(false);
        isShopActive = true;
        renderShop();
    }

    private void renderShop() {
        if (!isShopActive) return;
        shopGrid.getChildren().clear();

        for (int i = 0; i < shopCardCache.size(); i++) {
            Card card = shopCardCache.get(i);
            int row = (i < 6) ? 0 : 1;
            int col = (i < 6) ? i : (i - 6);
            if (card == null) {
                Region empty = new Region();
                empty.setPrefSize(100, 60);
                shopGrid.add(empty, col, row);
                continue;
            }
            Node cardNode = createCardNode(card);
            shopGrid.add(cardNode, col, row);
        }

        for (int i = 0; i < shopRelicCache.size(); i++) {
            Relic relic = shopRelicCache.get(i);
            if (relic == null) {
                Region empty = new Region();
                empty.setPrefSize(100, 60);
                shopGrid.add(empty, 2 + i, 1);
                continue;
            }
            Node relicNode = createRelicNode(relic);
            shopGrid.add(relicNode, 2 + i, 1);
        }

        Button bagBtn = new Button("打开背包");
        bagBtn.setPrefSize(240, 60);
        bagBtn.setStyle("-fx-background-color: yellow; -fx-font-weight: bold;");
        bagBtn.setOnAction(e -> onOpenFullInventory());
        shopGrid.add(bagBtn, 2, 2);
        GridPane.setColumnSpan(bagBtn, 3);
        GridPane.setHalignment(bagBtn, HPos.CENTER);

        Button discardBtn = new Button("购  买\n\n弃  牌");
        discardBtn.setText(remainingDiscardLeft <= 0 ? " (弃牌已达上限) " : " 弃一张牌\n" +
                PriceCalculator.getPriceForShopService(80, player.isHalfPrice()) + "金币" +
                "\n(剩" + remainingDiscardLeft + "次)");
        discardBtn.setDisable(remainingDiscardLeft <= 0);
        discardBtn.setOnAction(e -> onDiscardClick());
        discardBtn.setPrefSize(160, 300);
        discardBtn.setStyle("-fx-background-color: dodgerblue; -fx-font-size: 18; -fx-font-weight: bold;");
        discardBtn.setWrapText(true);
        shopGrid.add(discardBtn, 5, 1);
        GridPane.setRowSpan(discardBtn, 2);

        updatePlayerInfo();
    }

    public Node createCardNode(Card card) {
        int finalPrice = cardPriceMap.get(card);
        CardView view = CardView.forDisplay(card, CardViewSize.DISPLAY_MEDIUM);
        view.setHoverEffectEnabled(false);
        view.setOnMouseClicked(e -> onBuyCardClick(card));

        Label priceLabel = new Label("金币: " + finalPrice);
        priceLabel.setStyle("-fx-font-size: 14;");
        priceLabel.setMouseTransparent(true);

        VBox box = new VBox(view, priceLabel);
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(6);
        return box;
    }
    public Node createRelicNode(Relic relic) {
        Button button = new Button(relic.getName());
        button.setPrefSize(110, 100);
        button.setOnMouseClicked(e -> onBuyRelicClick(relic));
        int finalPrice = relicPriceMap.get(relic);
        Label price = new Label("价格: " + finalPrice);
        price.setStyle("-fx-font-size: 14;");

        VBox box = new VBox(button, price);
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(6);
        PopupHelper.bind(button, relic.getDescription());
        return box;
    }

    public List<Card> generateRandomCards() {
        List<Card> candidates = new ArrayList<>(allCards.values());
        Collections.shuffle(candidates);
        return new ArrayList<>(candidates.subList(0, Math.min(NUMBER_SHOW_CARDS, candidates.size())));
    }

    public List<Relic> generateRandomRelic() {
        List<Relic> owned = player.getRelicList();
        List<Relic> candidates = new ArrayList<>();
        List<Relic> relicsCopy = new ArrayList<>(allRelics);
        Collections.shuffle(relicsCopy);

        for (Relic relic : relicsCopy) {
            if (!owned.contains(relic)) candidates.add(relic);
        }
        List<Relic> result = new ArrayList<>(candidates.subList(0, Math.min(NUMBER_SHOW_RELIC, candidates.size())));

        // 不够再补
        if (result.size() < NUMBER_SHOW_RELIC) {
            for (Relic relic : relicsCopy) {
                if (result.contains(relic)) continue;
                result.add(relic);
                if (result.size() >= NUMBER_SHOW_RELIC) break;
            }
        }
        return result;
    }

    public void confirmAction(String name, String description, int price, Runnable onConfirm) {
        if (!isShopActive) return;
        OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                new ConfirmPurchaseOverlay(name, description, price, onConfirm)
        );
    }

    @FXML
    private void onBuyCardClick(Card card) {
        if (!isShopActive) return;
        if (deckManager.getAllCards().size() >= DeckManager.MAX_DECK) {
            showInfo("购买失败", "你的手牌已上限! ");
            return;
        }

        confirmAction(card.getName(), card.getDescription(), cardPriceMap.get(card), () -> {
            if (!tryPayGold(cardPriceMap.get(card))) return;
            deckManager.addCard(card);

            int index = shopCardCache.indexOf(card);
            if (index != -1) {
                shopCardCache.set(index, null);
            }

            renderShop();
            onUpdateUI();
        });
    }

    @FXML
    private void onBuyRelicClick(Relic relic) {
        if (!isShopActive) return;
        List<Relic> owned = player.getRelicList();
        if (owned.size() >= RelicFactory.RELIC_IN_TOTAL) {
            showInfo("购买失败", "你的宝物已达上限! ");
            return;
        }

        if (owned.contains(relic)) {
            showInfo("购买失败", "宝物已拥有! ");
            return;
        }

        confirmAction(relic.getName(), relic.getDescription(), relicPriceMap.get(relic), () -> {
            if (!tryPayGold(relicPriceMap.get(relic))) return;
            player.addAndEquipRelic(relic);

            int index = shopRelicCache.indexOf(relic);
            if (index != -1) {
                shopRelicCache.set(index, null);
            }

            renderShop();
            onUpdateUI();
        });
    }

    @FXML
    private void onRestClick() {
        if (!isShopActive) return;
        if (hasRested) {
            showInfo("已休息", "下一层可以再休息");
            return;
        }

        confirmAction("露营休息","准备于店长一起休息... (恢复50点HP)", 50, () -> {
            if (!tryPayGold(PriceCalculator.getPriceForShopService(100, player.isHalfPrice()))) return;
            player.healCharacter(100);
            hasRested = true;

            onUpdateUI();
            renderShop();
        });
    }

    @FXML
    private void onDiscardClick() {
        if (!isShopActive) return;
        if (remainingDiscardLeft <= 0) {
            showInfo("弃牌次数已用完", "每层只能弃牌3次！");
            return;
        }

        if (deckManager.getAllCards().isEmpty()) {
            showInfo("卡组为空", "没有可以弃的卡！");
            return;
        }

        showCardInventoryForDiscard();
    }

    @FXML
    private void onOpenFullInventory() {
        showFullInventory();
    }

    public void showInfo(String title, String content) {
        if (!isShopActive) return;
        OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                new InfoOverlay(title + "\n" + content, () -> {})
        );
    }

    public void showFullInventory() {
        if (!isShopActive) return;
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/zixun/cardGame/view/full-inventory.fxml"));
            Parent inventoryRoot = null;
            try {
                inventoryRoot = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            FullInventoryController controller = loader.getController();
            controller.initialize(player, deckManager.getAllCards(), player.getRelicList(), rootPane, overlayStack);

            BaseOverlay overlay = new BaseOverlay(600, 400);
            overlay.getChildren().add(inventoryRoot);
            controller.setOnCloseRequest(overlay::closeOverlay);
            return overlay;
        });
    }

    public void showCardInventoryForDiscard() {
        if (!isShopActive) return;
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/zixun/cardGame/view/card-inventory.fxml"));
            Parent cardInvRoot = null;
            try {
                cardInvRoot = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            DiscardCardInventoryController controller = loader.getController();
            controller.initialize(player, deckManager.getAllCards(), rootPane, overlayStack);
            controller.setDiscardMode(true);
            controller.setDiscardChanceLeft(remainingDiscardLeft);
            controller.setDiscardChanceLeftLabel(remainingDiscardLeft);
            controller.setOnCardClicked(card -> {
                if (!tryPayGold(PriceCalculator.getPriceForShopService(80, player.isHalfPrice()))) return;
                deckManager.removeCard(card);
                remainingDiscardLeft--;
                controller.setDiscardChanceLeft(remainingDiscardLeft);
                controller.setDiscardChanceLeftLabel(remainingDiscardLeft);
            });
            controller.setOnRefresh(() -> {
                controller.refresh(deckManager.getAllCards());
                renderShop();
                onUpdateUI();
            });

            BaseOverlay overlay = new BaseOverlay(600, 400);
            overlay.getChildren().add(cardInvRoot);
            controller.setOnCloseRequest(overlay::closeOverlay);

            return overlay;
        });
    }

    @FXML
    private void onClose() {
        if (onCloseRequest != null) {
            isShopActive = false;
            onCloseRequest.run(); // 让外部管理 overlay 关闭逻辑
        }
    }

    public void setOnCloseRequest(Runnable onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    private void setAllShopActionEnabled(boolean enabled) {
        removeCardButton.setDisable(!enabled);
    }

    public void onUpdateUI() {
        if (onUpdateUI != null) {
            onUpdateUI.run();
        }
    }

    public void setOnUpdateUI(Runnable onUpdateUI) {
        this.onUpdateUI = onUpdateUI;
    }

    private void updatePlayerInfo() {
        if (!isShopActive) return;
        goldLabel.setText("金币: " + player.getGold());
        shopHP.setText("HP: " + player.getHp() + " / " + player.getMaxHp());
    }

    public Boolean tryPayGold(int amount) {
        if (amount > player.getGold()) {
            showInfo("购买失败", "你的金币不足！");
            return false;
        } else {
            player.addGold(-amount);
            return true;
        }
    }
}
