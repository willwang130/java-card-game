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

public class RestPointOverlay extends BaseOverlay {

    private static final int WIDTH  = 600;
    private static final int HEIGHT = 420;

    private final Player player;
    private final DeckManager deckManager;
    private final StackPane rootPane;
    private final Deque<Node> overlayStack;
    private final Runnable updateUI;

    public RestPointOverlay(Player player,
                            DeckManager deckManager,
                            StackPane rootPane,
                            Deque<Node> overlayStack,
                            Runnable onClose) {
        super(WIDTH, HEIGHT);
        this.player      = player;
        this.deckManager = deckManager;
        this.rootPane     = rootPane;          // 赋值
        this.overlayStack = overlayStack;
        this.updateUI    = onClose;

        setOnClose(updateUI);

        // 初始显示「休息 / 铸造」选项
        getChildren().add(buildChoicePane());
    }

    /* ───────────────────────── 选择界面 ───────────────────────── */
    private Pane buildChoicePane() {
        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("休息点");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // ① 休息按钮
        Button restBtn = new Button("休息\n(+30 HP)");
        restBtn.setPrefSize(180, 90);
        restBtn.setStyle("-fx-font-size: 18px;");
        restBtn.setOnAction(e -> {
            player.addHp(30); // model 内已有 addHp()
            updateUI.run();
            closeOverlay();   // 直接关闭并返回地图
        });

        // ② 铸造按钮
        Button forgeBtn = new Button("铸造\n(升级卡牌)");
        forgeBtn.setPrefSize(180, 90);
        forgeBtn.setStyle("-fx-font-size: 18px;");
        forgeBtn.setOnAction(e ->
                OverlayUtil.openOverlay(rootPane, overlayStack,
                        () -> new ForgeOverlay(player, deckManager,
                                rootPane, overlayStack,updateUI)));

        HBox btnRow = new HBox(40, restBtn, forgeBtn);
        btnRow.setAlignment(Pos.CENTER);

        // 返回按钮（退出休息点，不可再进入）
        Button backBtn = new Button("返回");
        backBtn.setPrefSize(140, 60);
        backBtn.setStyle("-fx-font-size: 18px;");
        backBtn.setOnAction(e -> closeOverlay());
        VBox.setMargin(backBtn, new Insets(38, 0, 0, 0));

        root.getChildren().addAll(title, btnRow, backBtn);
        return root;
    }

    private void switchToForge() {
        getChildren().clear();
        getChildren().add(buildForgePane());
    }

    /* ───────────────────────── 铸造：选择卡牌 ───────────────────────── */
    private BorderPane buildForgePane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        Label title = new Label("升级");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        // 可升级卡牌
        FlowPane flow = new FlowPane();
        flow.setVgap(12);
        flow.setHgap(12);
        flow.setAlignment(Pos.TOP_LEFT);
        flow.setPrefWrapLength(WIDTH - 40);

        List<Card> upgradable = deckManager.getAllCards().stream()
                .filter(Card::canUpgrade)
                .toList();

        if (upgradable.isEmpty()) {
            // 没有可升级卡
            Label empty = new Label("没有可升级的卡牌");
            empty.setStyle("-fx-font-size: 18px;");
            flow.getChildren().add(empty);
        } else {
            for (Card card : upgradable) {
                CardView view = CardView.forDisplay(card, CardViewSize.DISPLAY_LARGE); // 适合 FlowPane 的缩略视图
                view.setOnMouseClicked(e -> showPreview(root, card));
                flow.getChildren().add(view);
            }
        }

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(HEIGHT - 120);
        root.setCenter(scroll);

        // 右上角关闭（返回上一层）
        Button close = createCloseButton(this::backToChoice);
        BorderPane.setMargin(close, new Insets(0, 0, 0, 0));
        BorderPane.setAlignment(close, Pos.TOP_RIGHT);
        root.setRight(close);

        return root;
    }

    private void backToChoice() {
        getChildren().clear();
        getChildren().add(buildChoicePane());
    }

    /* ───────────────────────── 铸造：预览与确认 ───────────────────────── */
    private void showPreview(BorderPane parent, Card baseCard) {
        Card upgraded = baseCard.createUpgradedCopy();

        // ① 左右两张卡牌的大图预览
        CardView leftCard  = CardView.forDisplay(baseCard, CardViewSize.DISPLAY_XL);
        CardView rightCard = CardView.forDisplay(upgraded, CardViewSize.DISPLAY_XL);

        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

        HBox preview = new HBox(24, leftCard, arrow, rightCard);
        preview.setAlignment(Pos.CENTER);

        // ② 确认升级按钮
        Button confirm = new Button("确定升级");
        confirm.setPrefSize(160, 60);
        confirm.setStyle("-fx-font-size: 20px;");
        confirm.setOnAction(e -> {
            baseCard.upgrade();
            closeOverlay(); // 升级完成后直接关闭返回地图
        });

        VBox box = new VBox(26);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("升级"), preview, confirm);
        VBox.setMargin(box.getChildren().get(0), new Insets(0,0,10,0));

        parent.setCenter(box);
    }
}
