package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Deque;
import java.util.List;


public class ForgeOverlay extends BaseOverlay {

    private static final int W = 1400, H = 900;

    private final Player      player;
    private final DeckManager deck;
    private final StackPane   rootPane;
    private final Deque<Node> overlayStack;
    private final Runnable    uiRefresh;

    public ForgeOverlay(Player      player,
                        DeckManager deck,
                        StackPane   rootPane,
                        Deque<Node> overlayStack,
                        Runnable    uiRefresh) {
        super(W, H);
        this.player       = player;
        this.deck         = deck;
        this.rootPane     = rootPane;
        this.overlayStack = overlayStack;
        this.uiRefresh    = uiRefresh;

        setOnClose(uiRefresh);               // 关闭时刷新 UI
        getChildren().add(buildContent());
    }

    /* ───────────────────── UI 构建 ───────────────────── */
    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        /* 顶部标题 & 关闭 */
        Label title = new Label("选择一张卡牌升级");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");
        Button close = BaseOverlay.createCloseButton(this::closeOverlay);

        BorderPane header = new BorderPane();
        header.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        header.setRight(close);
        root.setTop(header);


        TilePane tile = new TilePane();
        tile.setPrefColumns(6);
        tile.setHgap(60);
        tile.setVgap(60);
        tile.setPadding(new Insets(40));
        tile.setAlignment(Pos.TOP_CENTER);   // ⬅ 居中
        tile.setStyle("-fx-background-color: transparent;");

        List<Card> upgradable = deck.getAllCards().stream()
                .filter(Card::canUpgrade)
                .toList();

        if (upgradable.isEmpty()) {
            Label empty = new Label("当前没有可升级的卡牌");
            empty.setStyle("-fx-font-size:18px;");
            tile.getChildren().add(empty);
        } else {
            for (Card c : upgradable) {
                CardView view = CardView.forDisplay(c, CardViewSize.DISPLAY_LARGE);
                TilePane.setMargin(view, new Insets(5));
                // 保持可点击
                view.setOnMouseClicked(e -> openPreview(c));
                tile.getChildren().add(view);
            }
        }

        ScrollPane scroll = new ScrollPane(tile);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportWidth(H - 120);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding:0;");
        root.setCenter(scroll);

        return root;
    }

    /* ───────────────────── 事件 ───────────────────── */
    private void openPreview(Card baseCard) {
        OverlayUtil.openOverlay(rootPane, overlayStack,
                () -> new UpgradePreviewOverlay(baseCard, player, rootPane, overlayStack, uiRefresh));
    }
}
