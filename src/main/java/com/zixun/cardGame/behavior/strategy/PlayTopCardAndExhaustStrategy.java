package com.zixun.cardGame.behavior.strategy;

import com.zixun.cardGame.animation.CardAnimator;
import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.CardActionExecutor;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Character;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.view.CardView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PlayTopCardAndExhaustStrategy implements CardEffectStrategy{
    @Override
    public CardEffectResult execute(Object value, Card card, Character caster, Character target, GameEngine engine) {
        Card topCard = GameEngine.getInstance().getDeckManager().drawOneButPeek();
        if (topCard == null) return CardEffectResult.failed("你的抽牌堆是空的！");

        CardView temp = CardView.forAnimation(topCard, CardViewSize.BATTLE_HOVER); // 仅用来动画
        StackPane rootPane = engine.getController().getRootPane();
        Label deckLabel = engine.getController().getCardInDeckLabel();

        CardAnimator.flyDeckToCenterAndFade(rootPane, deckLabel, temp,
                () -> {
                    engine.onSetAllBattleButtons(false);
                },
                () -> {
                    engine.getCardActionExecutor().apply(topCard, caster, target);

                    engine.getDeckManager().exhaustCard(topCard);
                    engine.getController().renderHand();
                    engine.getController().updateBattleInfo();

                    engine.onSetAllBattleButtons(true);
                    engine.finishBattleIfNeeded();
                });

        return CardEffectResult.success("你打出了顶部卡牌并将其消耗: " + topCard.getName());
    }
}
