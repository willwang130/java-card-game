package com.zixun.cardGame.util;

import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.factory.CardFactory;
import com.zixun.cardGame.factory.RelicFactory;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.overlay.CardRewardOverlay;
import com.zixun.cardGame.model.overlay.RelicRewardOverlay;
import com.zixun.cardGame.model.relic.Relic;

import java.util.List;

public class BattleRewardProcessor {
    public enum RewardType { CARD_SELECTION, RELIC_SELECTION }

    public static void showRandomRewardEnemy(GameController controller) {
        RewardType type = Math.random() < 0.0 ? RewardType.CARD_SELECTION : RewardType.RELIC_SELECTION;
        switch (type) {
            case CARD_SELECTION -> showCardRewardOverlayEnemy(controller);
            case RELIC_SELECTION -> showRelicRewardOverlayEnemy(controller);
        }
    }

    public static void showRandomRewardLevelBoss(GameController controller) {
        RewardType type = Math.random() < 0.0 ? RewardType.CARD_SELECTION : RewardType.RELIC_SELECTION;
        switch (type) {
            case CARD_SELECTION -> showCardRewardOverlayLevelBoss(controller);
            case RELIC_SELECTION -> showRelicRewardOverlayLevelBoss(controller);
        }
    }


    public static void showCardRewardOverlayEnemy(GameController controller) {
        // 随机选出 3 张卡
        List<Card> rewardCards = CardFactory.getRandomCards(3);
        // 构造浮窗展示， 点击其中一张后加入 deckManager
        OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
            new CardRewardOverlay("胜利 你击败了敌人", "选择一张牌获得",
                    rewardCards, controller, controller::doBackToMap)
        );
    }

    public static void showCardRewardOverlayLevelBoss(GameController controller) {
        // 随机选出 3 张卡
        List<Card> rewardCards = CardFactory.getRandomCards(3);
        // 构造浮窗展示， 点击其中一张后加入 deckManager
        OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                new CardRewardOverlay("你击败了本层Boss！准备进入第 " +
                        (controller.getGameManager().getCurrentLevelNumber() + 1) +
                        " 层", "选择一张牌获得",
                        rewardCards, controller, controller::doBackToMap)
        );
    }

    public static void showRelicRewardOverlayEnemy(GameController controller) {
        // 从未取得的宝物里随机选出 2 个宝物，如果剩一个则另一个随机
        // 如果宝物已持有则直接获得卡牌价值的金钱并提示确认
        // 构造浮窗展示， 点击其中一个后加入 给player
        List<Relic> rewardRelics = RelicFactory.getRandomRelicsFromUnowned(
                2, controller.getPlayer().getRelicList());
        OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                new RelicRewardOverlay("胜利 你击败了敌人", "选择一个宝物获得",
                        rewardRelics, controller, controller::doBackToMap)
                );
    }
    public static void showRelicRewardOverlayLevelBoss(GameController controller) {
        List<Relic> rewardRelics = RelicFactory.getRandomRelicsFromUnowned(
                3, controller.getPlayer().getRelicList());
        OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                new RelicRewardOverlay("胜利 你击败了本层Boss", "选择一宝物获得",
                        rewardRelics, controller, controller::doBackToMap)
        );
    }
}
