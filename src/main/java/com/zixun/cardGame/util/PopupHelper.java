package com.zixun.cardGame.util;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Screen;

public class PopupHelper {

    private static final Popup POPUP = new Popup();          // 单例
    private static final Label CONTENT = new Label();        // 复用控件

    static {
        CONTENT.setStyle("""
            -fx-background-color: rgba(255,255,200,0.95);
            -fx-padding: 8 12;
            -fx-border-color: #777;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            -fx-font-size: 13;
        """);
        POPUP.getContent().add(CONTENT);
        POPUP.setAutoHide(true);                             // 点击其它区域自动关闭
    }

    /** 在 anchor 组件正下方 20px 处弹出；若越界则自动左移 */
    public static void show(Node anchor, String message) {
        if (message == null || message.isBlank()) return;

        CONTENT.setText(message);

        Bounds b   = anchor.localToScreen(anchor.getBoundsInLocal());
        double x   = b.getMinX() + 10;
        double y   = b.getMaxY() + 6;

        // 屏幕边界防溢出
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        if (x + POPUP.getWidth() > screen.getMaxX()) {
            x = screen.getMaxX() - POPUP.getWidth() - 12;
        }
        POPUP.show(anchor, x, y);
    }

    public static void hide() {
        POPUP.hide();
    }

    // 绑定：给任意 Node 添加 Hover popup
    public static void bind(Node node, String msg) {
        node.setOnMouseEntered(e -> PopupHelper.show(node, msg));
        node.setOnMouseExited(e -> PopupHelper.hide());
    }
}