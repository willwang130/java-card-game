package com.zixun.cardGame.model.overlay;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class BaseOverlay extends StackPane {

    private static final String STYLE = """
       -fx-background-color: #ffffff;
        -fx-border-color: #555;
        -fx-border-width: 2;
        -fx-background-radius: 10;
        -fx-border-radius: 10;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10,0,0,4);
    """;
    public BaseOverlay(double width, double height) {
        setPrefSize(width, height);
        setMaxSize(width, height);
        setStyle(STYLE);
    }

    public static Button createCloseButton(Runnable onClick) {
        Button btn = new Button("✕");
        btn.setOnAction(e -> onClick.run());
        btn.setFocusTraversable(false);
        btn.setStyle("""
        -fx-background-color: transparent;
        -fx-font-size: 20px;
        -fx-text-fill: #666;
        -fx-padding: 4 8 4 8;
    """);
        return btn;
    }

    // 关闭回调
    private Runnable onClose = () -> {};

    public void setOnClose(Runnable r) { onClose = r; }

    public void closeOverlay() { onClose.run(); }
}
