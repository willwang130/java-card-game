package com.zixun.cardGame.controller;

import com.zixun.cardGame.model.overlay.RelicDetailOverlay;
import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.util.OverlayUtil;
import com.zixun.cardGame.util.PopupHelper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class RelicInventoryController {
    @FXML
    private TilePane relicPane;

    private Consumer<Relic> onRelicClick;

    private List<Relic> relicList;

    private Runnable onCloseRequest;
    private StackPane rootPane;
    private Deque<Node> overlayStack;

    public void initialize(List<Relic> relicList, StackPane rootPane, Deque<Node> overlayStack) {
        this.relicList = new ArrayList<>(relicList);
        this.rootPane = rootPane;
        this.overlayStack = overlayStack;
        renderRelics();
    }

    private void renderRelics() {
        relicPane.getChildren().clear();
        for (Relic relic : relicList) {
            Label label = new Label(relic.getName() + "\n" + relic.getPrice() + "金币");
            label.setStyle("-fx-background-color: lightgoldenrodyellow;" +
                    " -fx-padding: 5 10 5 10; -fx-border-color: darkgoldenrod;");
            PopupHelper.bind(label, relic.getDescription());

            label.setOnMouseClicked(e -> {
                PopupHelper.hide();
                // 背包界面逻辑
                if (onRelicClick != null) {
                    onRelicClick.accept(relic);
                } else {
                    // 卖宝物界面逻辑
                    OverlayUtil.openOverlay(rootPane, overlayStack, () ->
                            new RelicDetailOverlay(relic));
                }
            });

            relicPane.getChildren().add(label);
        }
    }

    public void refresh(List<Relic> updatedCards) {
        this.relicList = new ArrayList<>(updatedCards);
        renderRelics();
    }

    public void setOnRelicClick(Consumer<Relic> handle) {
        this.onRelicClick = handle;
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
}
