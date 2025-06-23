package com.zixun.cardGame.animation;

import com.zixun.cardGame.model.card.Card;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import com.zixun.cardGame.view.CardView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CardAnimator {

    private final StackPane root;
    private final HBox handBox;
    private final Label deckLabel;      // 抽牌堆
    private final Label discardLabel;

    private final double spacing = 10;  // 手牌间距
    private final Function<Card, CardView> factory;   // 工厂接口
    private final Runnable renderHand;
    private static int seq = 0;
    private int pending = 0;      // 仍未结束的动画数量
    private Runnable onBatchDone; // 全批结束后的回调


    public CardAnimator(StackPane root, HBox handBox, Label deckLabel, Label discardLabel,
                        Function<Card, CardView> factory,
                        Runnable renderHand) {
        this.root = root;
        this.handBox = handBox;
        this.deckLabel = deckLabel;
        this.discardLabel  = discardLabel;
        this.factory = factory;
        this.renderHand = renderHand;
    }

    public void beginBatch(int total, Runnable onDone) {
        this.seq = 0;
        this.pending = total;
        this.onBatchDone = onDone;
    }

    public void flyDraw(Card card) {
        if (pending == 0) pending = 1;

        // 当前位置 + 排队序号
        int slot = handBox.getChildren().size() + seq;
        double delay = seq * 0.15;
        seq++;

        // ① 创建临时节点
        CardView temp = factory.apply(card);   // 无需 controller
        temp.setScaleX(0.01);temp.setScaleY(0.01);
        temp.setMouseTransparent(true);
        temp.setManaged(false);

        // ② 放到 rootPane 起点 = 牌堆中心
        Bounds deckScene = deckLabel.localToScene(deckLabel.getBoundsInLocal());
        Point2D start = root.sceneToLocal(
                deckScene.getMinX() + deckScene.getWidth() / 2,
                deckScene.getMinY() + deckScene.getHeight() / 2);

        // ③ 计算终点 = 手牌区下一空槽左上角
        Bounds handScene = handBox.localToScene(handBox.getBoundsInLocal());
        Point2D handBase = root.sceneToLocal(handScene.getMinX(), handScene.getMinY());

        double cardW = temp.getPrefWidth();
        double cardH = temp.getPrefHeight();

        double targetX = handBase.getX() + slot * (cardW + spacing) + cardW / 2;
        double targetY = handBase.getY() + cardH / 2;

        // 临时节点摆到起点
        temp.relocate(start.getX() - cardW / 2, start.getY() - cardH / 2);
        root.getChildren().add(temp); temp.toFront();

        // ④ 位移 + 缩放
        TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), temp);
        move.setToX(targetX - start.getX());
        move.setToY(targetY - start.getY());

        ScaleTransition grow = new ScaleTransition(Duration.seconds(0.3), temp);
        grow.setToX(1.0); grow.setToY(1.0);

        ParallelTransition fly = new ParallelTransition(move, grow);
        fly.setDelay(Duration.seconds(delay));

        fly.setOnFinished(ev -> {
            root.getChildren().remove(temp);  // 移除临时
            handBox.getChildren().add(factory.apply(card));

            // 完成计数
            if (--pending == 0 && onBatchDone != null) {
                onBatchDone.run();
                onBatchDone = null;   // 防重复调用
            }
        });
        fly.play();
    }

    public static void flyTo(StackPane rootPane, Node cardNode, Node targetNode, Runnable onFinished) {
        Bounds tgtScene = targetNode.localToScene(targetNode.getBoundsInLocal());
        Point2D tgtRoot = rootPane.sceneToLocal(
                tgtScene.getMinX() + tgtScene.getWidth()  / 2,
                tgtScene.getMinY() + tgtScene.getHeight() / 2);

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.15), cardNode);
        move.setToX(tgtRoot.getX() - cardNode.getLayoutX());
        move.setToY(tgtRoot.getY() - cardNode.getLayoutY());

        ScaleTransition shrink = new ScaleTransition(Duration.seconds(0.15), cardNode);
        shrink.setToX(0.1); shrink.setToY(0.1);

        ParallelTransition fly = new ParallelTransition(move, shrink);
        fly.setOnFinished(ev -> onFinished.run());
        fly.play();
    }

    /**
     * 将牌飞向弃牌堆。
     */
    public void flyToDiscard(Node cardNode, Runnable onFinished) {
        flyTo(root, cardNode, discardLabel, () -> {
            root.getChildren().remove(cardNode);
            if (onFinished != null) onFinished.run();
        });
    }

    /* ========== ① 单张：从 handBox ➜ 弃牌堆 ========== */
    private Animation buildFlyOneToDiscard(Node cardNode, Runnable after) {
        // 用已有 flyTo
        Bounds tgtScene = discardLabel.localToScene(discardLabel.getBoundsInLocal());
        Point2D tgt = root.sceneToLocal(
                tgtScene.getMinX() + tgtScene.getWidth()  / 2,
                tgtScene.getMinY() + tgtScene.getHeight() / 2);

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.1), cardNode);
        move.setToX(tgt.getX() - cardNode.getLayoutX());
        move.setToY(tgt.getY() - cardNode.getLayoutY());

        ScaleTransition shrink = new ScaleTransition(Duration.seconds(0.1), cardNode);
        shrink.setToX(0.1); shrink.setToY(0.1);

        ParallelTransition p = new ParallelTransition(move, shrink);
        p.setOnFinished(e -> {
            root.getChildren().remove(cardNode);   // 动画完从 rootPane 删掉
            if (after != null) after.run();
        });
        return p;
    }

    public void fadeAndRemove(Node cardNode, Runnable onFinished) {
        cardNode.setMouseTransparent(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(1.2), cardNode);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            Pane parent = (Pane) cardNode.getParent();
            if (parent != null) parent.getChildren().remove(cardNode);
            if (onFinished != null) onFinished.run();
        });
        ft.play();
    }


    /* ========== 批量：按顺序飞弃牌堆 ========== */
    public void animateDiscardHand(HBox handBox,
                                   Consumer<CardView> perCardLogic,   // 每张牌逻辑（可为 null）
                                   Runnable whenDone) {     // 全部结束后回调
        // 拷一份列表，避免动画过程中 handBox 改变大小
        List<Node> cards = new ArrayList<>(handBox.getChildren());
        if (cards.isEmpty()) { if (whenDone != null) whenDone.run(); return; }

        // 按索引顺序依次执行动画
        final int[] idx = {0};
        Runnable[] playNext = new Runnable[1];
        playNext[0] = () -> {
            if (idx[0] >= cards.size()) {
                if (whenDone != null) whenDone.run();
                return;
            }
            CardView cardView = (CardView) cards.get(idx[0]++);
            // 记录当前屏幕位置
            Point2D scenePos = cardView.localToScene(0, 0);

            // 先把它从 handBox 转到 rootPane 顶层
            handBox.getChildren().remove(cardView);
            cardView.setManaged(false);
            root.getChildren().add(cardView); cardView.toFront();

            /* 复位到原屏幕位置 */
            Point2D rootPos = root.sceneToLocal(scenePos);
            cardView.relocate(rootPos.getX(), rootPos.getY());

            Animation anim = buildFlyOneToDiscard(cardView, () -> {
                if (perCardLogic != null) perCardLogic.accept(cardView);
                playNext[0].run();  // 递归下一张
            });
            anim.play();
        };
        playNext[0].run();
    }
    /** 拖拽取消 / 使用失败：将牌飞回 returnPos，并在回调里交还手牌槽 */
    public void flyBackToHand(StackPane rootPane,
                              CardView cardNode,
                              Point2D returnPosInRoot,
                              Runnable onFinished) {

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.25), cardNode);
        move.setToX(returnPosInRoot.getX() - cardNode.getLayoutX());
        move.setToY(returnPosInRoot.getY() - cardNode.getLayoutY());

        ScaleTransition shrink = new ScaleTransition(Duration.seconds(0.25), cardNode);
        shrink.setToX(1); shrink.setToY(1);

        ParallelTransition back = new ParallelTransition(move, shrink);
        back.setOnFinished(e -> {
            cardNode.setMouseTransparent(false);
            if (onFinished != null) onFinished.run();
        });
        back.play();
    }

    public static void flyDeckToCenterAndFade(StackPane root,
                                              Node deckLabel,
                                              Node tempCard,
                                              Runnable after) {
        flyDeckToCenterAndFade(root, deckLabel, tempCard, null, after);
    }

    public static void flyDeckToCenterAndFade(StackPane root,
                                              Node deckLabel,
                                              Node tempCard,
                                              Runnable before,
                                              Runnable after) {
        if (before != null) before.run();

        /* 起点 = 抽牌堆中心 */
        Bounds srcScene = deckLabel.localToScene(deckLabel.getBoundsInLocal());
        Point2D src = root.sceneToLocal(
                srcScene.getMinX() + srcScene.getWidth() / 2,
                srcScene.getMinY() + srcScene.getHeight() / 2);

        /* 终点 = rootPane 正中 */
        double dstX = root.getWidth()  / 2;
        double dstY = root.getHeight() / 2;

        /* 准备节点 */
        tempCard.setScaleX(0.01); tempCard.setScaleY(0.01);        // 与 flyDraw 一致
        tempCard.relocate(src.getX() - tempCard.prefWidth(-1)/2,
                src.getY() - tempCard.prefHeight(-1)/2);
        root.getChildren().add(tempCard); tempCard.toFront();

        /* 飞行 + 放大：0.3s */
        TranslateTransition move = new TranslateTransition(Duration.seconds(0.2), tempCard);
        move.setToX(dstX - src.getX());
        move.setToY(dstY - src.getY());

        ScaleTransition grow = new ScaleTransition(Duration.seconds(0.2), tempCard);
        grow.setToX(1.5); grow.setToY(1.5);

        ParallelTransition fly = new ParallelTransition(move, grow);

        /* 停留 */
        PauseTransition pause = new PauseTransition(Duration.seconds(0.1));

        /* 淡出：沿用 fadeAndRemove 的 1.2s */
        FadeTransition fade = new FadeTransition(Duration.seconds(0.6), tempCard);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        SequentialTransition seq = new SequentialTransition(fly, pause, fade);
        seq.setOnFinished(e -> {
            root.getChildren().remove(tempCard);
            if (after != null) after.run();
        });
        seq.play();
    }

    // 抽牌顺序复位（每回合开始调用）
    public static void resetSeq() { seq = 0; }
}
