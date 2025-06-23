package com.zixun.cardGame.model.overlay;

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

public class PileOverlay extends BaseOverlay {

    public PileOverlay(String title,
                       List<Card> pile,
                       Player player,
                       StackPane rootPane,
                       Deque<Node> overlayStack) {

        super(1280, 840);                       // ↖ 继承 BaseOverlay 的宽高与阴影:contentReference[oaicite:1]{index=1}

        // 头部标题
        Label titleLabel = new Label(title + " (" + pile.size() + ")");
        titleLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        // 关闭按钮
        Button closeBtn = BaseOverlay.createCloseButton(this::closeOverlay);

        BorderPane header = new BorderPane();
        header.setCenter(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);     // 居中
        header.setRight(closeBtn);

        // 牌列表
        TilePane tile = new TilePane(12, 12);   // 10px 间距
        tile.setPrefColumns(6);
        tile.setPrefTileHeight(240);
        tile.setPrefRows(2);
        tile.setPadding(new Insets(16));
        tile.setStyle("-fx-background-color: white;");

        for (Card c : pile) {
            CardView view = CardView.forDisplay(c, CardViewSize.DISPLAY_LARGE);    // 复用现成 CardView
            view.setOnMouseClicked(e -> OverlayUtil.openOverlay(
                    rootPane, overlayStack, () -> new CardDetailOverlay(c, player)  // 单卡放大:contentReference[oaicite:2]{index=2}
            ));
            tile.getChildren().add(view);
        }

        ScrollPane scroll = new ScrollPane(tile);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("""
                    -fx-background-color: transparent;
                    -fx-background-insets: 0;
                    -fx-padding: 0;
                    -fx-border-color: transparent;
                    -fx-border-width: 0;
                """);

        VBox body = new VBox(20, header, scroll);
        body.setPadding(new Insets(28));

        getChildren().add(body);
        setAlignment(Pos.CENTER);
    }
}