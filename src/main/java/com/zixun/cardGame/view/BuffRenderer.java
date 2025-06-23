package com.zixun.cardGame.view;

import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.ActiveAbility;
import com.zixun.cardGame.type.StatusNames;
import com.zixun.cardGame.util.PopupHelper;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class BuffRenderer {

    public static void renderAll(Character caster, FlowPane targetBox) {
        targetBox.getChildren().clear();

        // 能力 单独显示
        for (ActiveAbility ab : caster.getActiveAbilities()) {
            Label tag = createTag(ab.name(), "#9c7dff", ab.description());
            targetBox.getChildren().add(tag);
        }

        // 普通 Buff / DeBuff
        caster.getStatusManager().getAll().forEach((status, value) -> {
            if (value == 0) return;

            String label = StatusNames.getChineseFromStatus(status) + " " + value;
            String text = StatusNames.getDescription(status);
            String color = status.isDeBuff()  ? "#e57373"
                    : "#66bb6a";
            targetBox.getChildren().add(createTag(label, color, text));
        });
    }
    private static Label createTag(String text, String bgColor, String tooltipText) {
        Label label = new Label(text);
        label.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 2 6;
            -fx-background-radius: 8;
            -fx-font-size: 18;
        """, bgColor));

        if (tooltipText != null && !tooltipText.isBlank()) {
            PopupHelper.bind(label, tooltipText);
        }

        return label;
    }
}
