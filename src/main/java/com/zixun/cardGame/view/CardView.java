package com.zixun.cardGame.view;

import com.zixun.cardGame.model.card.Card;

import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.util.CombatCalculator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardView extends VBox {
    private final Card card;
    private Character hoverTarget;
    private Player player;
    private TextFlow descFlow;
    private boolean hoverEffectEnabled = false;

    private CardViewSize size;
//
//    private double w = 100;
//    private double h = 150;

    private CardView(Card card, Player player) {
        this.card = card;
        this.player = player;
        if (player != null) {
            player.getStatusManager().addListener(this::updateDescription);
        }
    }
    public static CardView forDisplay(Card card, CardViewSize size) {
        CardView view = new CardView(card, null);
        view.resizeTo(size);
        return view;
    }

    public static CardView forCombat(Card card, Player player, CardViewSize size) {
        CardView view = new CardView(card, player);
        view.resizeTo(size);
        return view;
    }

    public static CardView forAnimation(Card card, CardViewSize size) {
        CardView view = new CardView(card, null);
        view.setManaged(false);
        view.setMouseTransparent(true);
        view.resizeTo(size);
        return view;
    }


    public void resizeTo(CardViewSize size) {
        this.size = size;
        this.setPrefSize(size.width, size.height);
        this.setMaxSize(size.width, size.height);
        renderCard();
    }

    private void renderCard() {
        double w = size.width, h = size.height;
        this.getChildren().clear();
        // 阴影
        Rectangle shadow = new Rectangle(w, h);
        shadow.setArcWidth(18); shadow.setArcHeight(18);
        shadow.setFill(Color.rgb(0, 0, 0, 0.3));
        shadow.setTranslateY(4);                // 厚度感
        shadow.setMouseTransparent(true);

        // 主体
        Rectangle body = new Rectangle(w - 4, h - 4);
        body.setArcWidth(18); body.setArcHeight(18);
        body.setFill(createRarityGradient(card.getRarity()));
        body.setStroke(Color.web("#555"));      // 细描边

        // 文本区
        Label epLabel   = new Label("EP: " + card.getCost());
        Label nameLabel = new Label(displayName(card));
        Label typeLabel = new Label(card.getType());
        descFlow  = createDynamicDescriptionTextFlow();
        TextFlow descriptionFlow = descFlow;

        // 行内样式
        epLabel.setAlignment(Pos.CENTER_LEFT);
        epLabel.setMaxWidth(Double.MAX_VALUE);
        epLabel.setPadding(new Insets(4, 0, 0, 8)); // 贴左再留 4px

        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-size: 15px");

        typeLabel.setAlignment(Pos.CENTER);
        typeLabel.setMaxWidth(Double.MAX_VALUE);
        typeLabel.setStyle("-fx-font-size: 10px");

        descriptionFlow .setTextAlignment(TextAlignment.CENTER);
        descriptionFlow .setPrefWidth(w - 14);

        // 2个弹性spacers ---------------------------------------
        Region spacerTop    = new Region();
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerTop,    Priority.ALWAYS);
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        VBox headerBox = new VBox(0, epLabel, nameLabel, typeLabel);
        headerBox.setAlignment(Pos.TOP_CENTER);

        StackPane descPane = new StackPane(descriptionFlow);
        descPane.setAlignment(Pos.CENTER);
        VBox.setVgrow(descPane, Priority.ALWAYS); // 吃掉余高

        VBox content = new VBox(headerBox, spacerTop, descPane, spacerBottom);
        content.setAlignment(Pos.TOP_CENTER);

        StackPane cardPane = new StackPane(shadow, body, content);
        cardPane.setPrefSize(w, h);
        cardPane.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.45)));

        this.setPrefSize(w, h);     // ★ 明确限制 CardView 自身大小
        this.setMaxSize(w, h);      // （可选）限制不能自动拉伸
        this.setPickOnBounds(false);

        getChildren().add(cardPane);
    }

    private TextFlow createDynamicDescriptionTextFlow() {
        int baseDamage = card.extractDamageBase();
        int modDamage = (player == null) ? baseDamage :
                (hoverTarget != null)
                        ? CombatCalculator.calculateFinalDamage(player, hoverTarget, baseDamage)
                        : CombatCalculator.calcAttack(baseDamage, player);

        int baseBlock = card.extractBlockBase();
        int modBlock = (player == null) ? baseBlock
                : CombatCalculator.calcBlock(baseBlock, player);

        int total = (player != null && card.getAction().containsKey("xCost"))
                ? modDamage * player.getEp()
                : -1;
        String full  = resolvePlaceholders(card.getDescription(), modDamage, modBlock, total);

        return renderColored(full, baseDamage, modDamage, baseBlock, modBlock, total);
    }

    public static String createDynamicDescriptionString(Card card, Player player) {

        int baseDamage = card.extractDamageBase();
        int modDamage = CombatCalculator.calcAttack(baseDamage, player);

        int baseBlock = card.extractBlockBase();
        int modBlock = CombatCalculator.calcBlock(baseBlock, player);

        int total = 0;

        return resolvePlaceholders(card.getDescription(), modDamage, modBlock, total);
    }

    private static String resolvePlaceholders(String template, int damage, int block, int total) {
        return template
                .replace("{damage}", String.valueOf(damage))
                .replace("{block}",  String.valueOf(block))
                .replace("{total}",  total >= 0 ? String.valueOf(total) : "");
    }

    private Paint createRarityGradient(String r) {
        Color base = switch (r) {
            case "common" -> Color.web("#ececec");
            case "uncommon" -> Color.web("#a0e0ff");
            case "rare" -> Color.web("#ffd166");
            default -> Color.web("#e76f51");
        };
        return new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, base.brighter()), new Stop(1, base.darker()));
    }

    public void setHoverTarget(Character target) {
        this.hoverTarget = target;
    }

    public void updateDescription() {
        if (descFlow == null) return;                     // 还没渲染完
        TextFlow fresh = createDynamicDescriptionTextFlow();
        descFlow.getChildren().setAll(fresh.getChildren());  // 只替换子节点
    }

    // 静态工具 统一渲染 {damage} {block} 与卡牌显示名
    public static String displayName(Card card) {
        return card.getName() + (card.getLevel() == 0 ? "" : "+");
    }

    // 根据基准值与修正值渲染彩色
    private static final Pattern PLACEHOLDER =
            Pattern.compile("\\{damage}|\\{block}|\\{total}");

    public static TextFlow renderColored(String template,
                                         int baseDamage, int actualDamage,
                                         int baseBlock,  int actualBlock,
                                         int totalValue) {
        TextFlow flow = new TextFlow();
        Matcher matcher = PLACEHOLDER.matcher(template);
        int last = 0;

        while (matcher.find()) {
            // 先加前一段纯文本
            flow.getChildren().add(new Text(template.substring(last, matcher.start())));
            String ph = matcher.group();

            switch (ph) {
                case "{damage}" -> flow.getChildren().add(
                        styledNumber(String.valueOf(actualDamage), baseDamage, actualDamage));

                case "{block}"  -> flow.getChildren().add(
                        styledNumber(String.valueOf(actualBlock), baseBlock, actualBlock));

                case "{total}"  -> flow.getChildren().add(
                        new Text(String.valueOf(totalValue)));
            }
            last = matcher.end();
        }
        // 结尾剩余文本
        flow.getChildren().add(new Text(template.substring(last)));
        return flow;
    }

    // 根据增减上色
    private static Text styledNumber(String num, int base, int actual) {
        Text t = new Text(num);
        if (actual > base) t.setFill(Color.GREEN);
        else if (actual < base) t.setFill(Color.DARKRED);
        t.setStyle("-fx-font-weight: bold");
        return t;
    }


    public void setHoverEffectEnabled(boolean enabled) {
        this.hoverEffectEnabled  = !enabled;
        if (enabled) {
            setOnMouseEntered(e -> {
                if (!hoverEffectEnabled ) {
                    setScaleX(1.30);
                    setScaleY(1.30);
                }
            });
            setOnMouseExited(e -> {
                if (!hoverEffectEnabled ) {
                    setScaleX(1.0);
                    setScaleY(1.0);
                }
            });
        }
    }
    public Card getCard() {
        return card;
    }
}