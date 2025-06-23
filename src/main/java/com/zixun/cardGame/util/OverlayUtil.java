package com.zixun.cardGame.util;

import com.zixun.cardGame.model.overlay.BaseOverlay;
import com.zixun.cardGame.model.overlay.InfoOverlay;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Deque;
import java.util.function.Supplier;

public class OverlayUtil {
    public static void centerNode(Pane root, Node n) {
        double x = (root.getWidth() - n.prefWidth(-1)) / 2;
        double y = (root.getHeight() - n.prefHeight(-1)) / 2;
        n.setLayoutX(x);
        n.setLayoutY(y);
    }

    public static void centerNode(Node n) {
        if (n.getScene() == null || !(n.getScene().getRoot() instanceof Pane root)) return;
        centerNode(root, n);
    }

    public static Pane makeDimPane(Pane root) {
        Pane dim = new Pane();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.35);");   // 全屏灰
        dim.prefWidthProperty().bind(root.widthProperty());
        dim.prefHeightProperty().bind(root.heightProperty());
        return dim;
    }

    public static Pane makeDimPane() {
        Pane dim = new Pane();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        dim.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        StackPane.setAlignment(dim, Pos.CENTER);
        return dim;
    }

    public static <T extends BaseOverlay> T openOverlay(StackPane rootPane, Deque<Node> overlayStack, Supplier<T> supplier) {
        Pane dimPane = OverlayUtil.makeDimPane(rootPane);
        T overlay = supplier.get();
        OverlayUtil.centerNode(overlay);

        overlay.setOnClose(() -> {
            popOverlay(rootPane, overlayStack, overlay);
            popOverlay(rootPane, overlayStack, dimPane);
        });

        pushOverlay(rootPane, overlayStack, dimPane);
        pushOverlay(rootPane, overlayStack, overlay);
        return overlay;
    }

    public static void pushOverlay(StackPane rootPane, Deque<Node> overlayStack, Node node) {
        overlayStack.push(node);
        rootPane.getChildren().add(node);
    }

    public static void popOverlay(StackPane rootPane, Deque<Node> overlayStack, Node node) {
        overlayStack.remove(node);
        rootPane.getChildren().remove(node);
    }

    public static void showInfo(StackPane rootPane, Deque<Node> overlayStack, String message) {
        openOverlay(rootPane, overlayStack, () -> new InfoOverlay(message, () -> {}));
    }
}


//    private void centerNode(Node n) {
//        double x = (rootPane.getWidth()  - n.prefWidth(-1))  / 2;
//        double y = (rootPane.getHeight() - n.prefHeight(-1)) / 2;
//        n.setLayoutX(x);
//        n.setLayoutY(y);
//    }

//private Pane makeDimPane() {
//    Pane dim = new Pane();
//    dim.setStyle("-fx-background-color: rgba(0,0,0,0.35);");   // 全屏灰
//    dim.prefWidthProperty().bind(rootPane.widthProperty());
//    dim.prefHeightProperty().bind(rootPane.heightProperty());
//    return dim;
//}