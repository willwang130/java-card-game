package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.view.CardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Deque;

public class UpgradePreviewOverlay extends BaseOverlay {

    private static final int W = 1360, H = 800;

    private final Card baseCard;
    private final Player player;
    private final StackPane rootPane;      // 仅用于后续可能再开覆盖层
    private final Deque<Node> overlayStack;  // 同上
    private final Runnable updateUI;     // 关闭时刷新 UI


    UpgradePreviewOverlay(Card baseCard,
                          Player player,
                          StackPane rootPane,
                          Deque<Node> overlayStack,
                          Runnable updateUI) {

        super(W, H);
        this.baseCard = baseCard;
        this.player = player;
        this.rootPane = rootPane;
        this.overlayStack = overlayStack;
        this.updateUI = updateUI;

        setOnClose(updateUI);

        getChildren().add(buildContent());
    }

    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPrefSize(W, H);
        root.setPadding(new Insets(30));

        // 顶部标题 & 关闭
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.setSpacing(20);
        Label title = new Label("升级预览");
        title.setStyle("-fx-font-size:24px; -fx-font-weight:bold;");
        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);
        Button closeBtn = BaseOverlay.createCloseButton(this::closeOverlay);
        topBar.getChildren().addAll(spacerLeft, title, spacerRight, closeBtn);
        root.setTop(topBar);

        // 左右两张卡牌
        HBox centerBox = new HBox(60);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPrefHeight(300);
        CardView left = CardView.forDisplay(baseCard, CardViewSize.DISPLAY_XL);
        CardView right = CardView.forDisplay(baseCard.createUpgradedCopy(), CardViewSize.DISPLAY_XL);
        left.setMouseTransparent(true);
        right.setMouseTransparent(true);
        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size:42px; -fx-font-weight:bold;");
        centerBox.getChildren().addAll(left, arrow, right);
        root.setCenter(centerBox);

        // 底部 确定升级
        Button confirm = new Button("确定升级");
        confirm.setPrefSize(220, 60);
        confirm.setStyle("-fx-font-size:18px;");

        confirm.setOnAction(e -> {
            baseCard.upgrade();
            updateUI.run();      // ① 先刷新顶栏 / 地图
            // ② 依次关闭自己、ForgeOverlay、RestPointOverlay
            closeOverlay();                                   // 关预览
            overlayStack.stream()                             // 关 Forge
                    .filter(n -> n instanceof BaseOverlay && n != this)
                    .forEach(n -> ((BaseOverlay) n).closeOverlay());
        });

        StackPane bottomPane = new StackPane(confirm);
        bottomPane.setPadding(new Insets(30, 0, 0, 0));
        root.setBottom(bottomPane);

        closeBtn.toFront();

        return root;
    }
}