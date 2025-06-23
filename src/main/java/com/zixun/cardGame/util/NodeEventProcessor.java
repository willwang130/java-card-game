package com.zixun.cardGame.util;

import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.factory.CardFactory;
import com.zixun.cardGame.factory.RelicFactory;
import com.zixun.cardGame.map.MapNode;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.overlay.CardRewardOverlay;
import com.zixun.cardGame.model.overlay.RestPointOverlay;
import com.zixun.cardGame.model.relic.Relic;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.zixun.cardGame.type.NodeContentEnum.NONE;
import static com.zixun.cardGame.type.NodeContentEnum.START_EVENT;

public class NodeEventProcessor {
    public static void processNode(GameController controller, MapNode node) {
        System.out.println("Enter Node of Content: " + node.content.toString() + "\n");
        if (node.content == START_EVENT) {
            Log.write("你踏上了旅程的起点……\n");
            controller.showInfoDialog(
                    "新的冒险开始！",
                    "（这里写你想要的起点奖励或剧情，例如：恢复 30 HP 并获得 50 金币）",
                    Map.of("出发！", controller::doBackToMap)
            );
        }

        switch (node.content) {
            case ENEMY -> {
                Log.write("遭遇敌人，进入战斗！\n");
                controller.startBattle(node.monster);
            }
            case MINI_BOSS -> {
                Log.write("遭遇小Boss，进入战斗！\n");
                controller.startBattle(node.monster);
            }
            case FINAL_BOSS -> {
                Log.write("你遇到了最终Boss！决战开始！");
                controller.startBattle(node.monster);
            }
            case EVENT -> {
                double random = Math.random() + 20;
                eventGetCard(controller);
                if (random < 0.10) {
                    eventBoostEP(controller);
                } else if (random < 0.28) {
                    eventBoostAndRestPlayer(controller);
                } else if (random < 0.46) {
                    eventGetCard(controller);
                } else if (random < 0.64) {
                    eventGetRelic(controller);
                } else if (random < 0.82) {
                    eventGetGold(controller);
                } else if (random < 1.0) {
                    eventShopDiscount(controller);
                }
            }
            case TREASURE -> {
                Log.write("你打开宝箱发现了...！\n");
                Relic relic = controller.getPlayer().addNewRandomRelic();
                if (relic == null) {
                    eventTreasureGold(controller);
                } else {
                    controller.showInfoDialog("你发现了宝藏！", "芜湖！(你获得了 " + relic.getName() + " )", Map.of(
                            "确认", controller::doBackToMap));
                }
            }
            case REST -> {
                OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(),
                        () -> // 重新渲染地图 / 状态
                                new RestPointOverlay(controller.getPlayer(),
                                        controller.getDeckManager(),
                                        controller.getRootPane(),
                                        controller.getOverlayStack(),
                                        controller::updateUI));
            }
            case SHOP -> {
                Log.write("发现商店！");
                controller.openShop();
                return;
            }
            case START_EVENT -> {
                Log.write("你睁开了眼, 发现一个人");
            }
            default -> {
                Log.write("安全地移动到了新位置。\n");
            }
        }
        //node.content = NONE;
    }

    private static void eventBoostAndRestPlayer(GameController controller) {
        Log.write("发生了奇怪的事件...\n");
        controller.getPlayer().healCharacter(80);
        controller.showInfoDialog("发生了奇怪的事件...", "恢复了80点血量！", Map.of(
                "确认", controller::doBackToMap
        ));
    }

    private static void eventGetRelic(GameController controller) {
        Log.write("你伸手去碰墙壁裂缝中的闪闪发光...\n");
        List<Relic> randomRelics = RelicFactory.getRandomRelicsFromUnowned(
                1, controller.getPlayer().getRelicList());
        // 如获得所以宝物则改为获得金币事件
        if (randomRelics.isEmpty()) {
            eventGetGold(controller);
            return;
        }
        Relic chosen = randomRelics.get(0);
        boolean success = controller.getPlayer().addAndEquipRelic(chosen);
        if (!success) {
            // controller.appendLog("你已经拥有这个宝物了，它化为了金币...\n");
            eventGetGold(controller);
            return;
        }
        controller.showInfoDialog("神秘事件：裂缝中的宝物",
                "获得了宝物 (" + randomRelics.get(0).getName() +")！", Map.of(
                "确认", controller::doBackToMap
        ));
    }
    private static void eventGetCard(GameController controller) {
        List<Card> rewardCards = CardFactory.getRandomCards(3);
        Log.write("你在地上发现了几张卡牌：");

        OverlayUtil.openOverlay(controller.getRootPane(), controller.getOverlayStack(), () ->
                new CardRewardOverlay("神秘事件：卡牌选择", "你在地上发现了几张卡牌，选择一张获得：",
                        rewardCards, controller, controller::doBackToMap)
        );
    }
    private static void eventGetGold(GameController controller) {
        int gold = 60 + new Random().nextInt(41); // 60~100
        controller.getPlayer().addGold(gold);
        Log.write("你捡到了一袋金币 + " + gold + "\n");

        controller.showInfoDialog("神秘事件：金币袋",
                "你拾起了一袋金币，获得了 " + gold + " 金币！",
                Map.of("确认", controller::doBackToMap));
    }
    private static void eventTreasureGold(GameController controller) {
        int gold = 120 + new Random().nextInt(81); // 120~200
        controller.getPlayer().addGold(gold);
        Log.write("你打开了宝箱 + " + gold + "\n");

        controller.showInfoDialog("打开宝箱：一堆金币!",
                "你拾起了金币，获得了 " + gold + " 金币！",
                Map.of("确认", controller::doBackToMap));
    }
    private static void eventShopDiscount(GameController controller) {
        Log.write("你遇到了神秘商人，所有商品打5折！\n");
        controller.getPlayer().setShopDiscount(true);  // 你自己实现这个状态变量
        controller.openShop();
    }
    private static void eventBoostEP(GameController controller) {
        controller.getPlayer().addMaxEp(1);
        Log.write("你感受到了一股神秘力量，最大能量上限提升！\n");
        controller.showInfoDialog("神秘事件：神秘力量",
                "最大能量上限 +1！",
                Map.of("确认", controller::doBackToMap));
    }
}
