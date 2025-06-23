package com.zixun.cardGame.model.overlay;

import com.zixun.cardGame.controller.RelicInventoryController;
import com.zixun.cardGame.model.relic.Relic;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Supplier;

public class RelicSellOverlay extends BaseOverlay{
    public RelicSellOverlay(Relic relic, RelicInventoryController controller,
                            Runnable onConfirmSell, Supplier<List<Relic>> relicsSupplier,
                            Runnable refreshMapRelics) {
        super(400, 300);

        Label name = new Label(relic.getName());
        Label desc = new Label(relic.getDescription());
        Label price = new Label("售价：" + relic.getPrice());

        Button confirm = new Button("确认出售");
        confirm.setOnAction(e -> {
            onConfirmSell.run();
            controller.refresh(relicsSupplier.get());
            refreshMapRelics.run();
            this.closeOverlay();
        });

        Button cancel = new Button("取消");
        cancel.setOnAction(e -> this.closeOverlay());

        VBox content = new VBox(10, name, desc, price, confirm, cancel);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        getChildren().add(content);
    }
}
