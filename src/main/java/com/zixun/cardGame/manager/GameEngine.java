
package com.zixun.cardGame.manager;
import com.zixun.cardGame.animation.CardAnimator;
import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.behavior.CardHandlerChain;
import com.zixun.cardGame.behavior.EpCheck;
import com.zixun.cardGame.behavior.MonsterAction;
import com.zixun.cardGame.controller.GameController;
import com.zixun.cardGame.observer.EventManager;
import com.zixun.cardGame.event.GameStateChangedEvent;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.model.character.Player;
import com.zixun.cardGame.type.GameEngineState;
import com.zixun.cardGame.type.SourceType;
import com.zixun.cardGame.util.CombatCalculator;
import javafx.application.Platform;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import static com.zixun.cardGame.type.GameEngineState.*;
import static com.zixun.cardGame.type.TriggerTypes.*;


public class GameEngine {
    private static final GameEngine INSTANCE = new GameEngine();

    private Player player;
    private Monster monster;
    private int levelIdx;
    private DeckManager deckManager; // 持有卡组
    private GameController controller;
    private int roundCount = 1;
    private GameEngineState state = MAP_TURN_ENGINE;
    private GameEngineState stateBeforePause;
    private Consumer<Boolean> setAllBattleButtonsConsumer;
    public static boolean drawingNow = false;

    private final TriggerManager triggerManager = TriggerManager.getInstance();
    private final CardActionExecutor cardActionExecutor = new CardActionExecutor();

    private static final CardHandlerChain DEFAULT_CHAIN =
            new EpCheck(new CardActionExecutor());

    private GameEngine() {};

    public void initialize(Player player, Monster monster, int levelIdx, DeckManager deckManager, GameController controller) {
        this.player = player;
        this.monster = monster;
        this.levelIdx = levelIdx;
        this.roundCount = 1;
        this.deckManager = deckManager;
        this.controller = controller;
        this.state = PLAYER_TURN;

        startBattle();
    }

    public void startBattle() {
        // 重置
        triggerManager.clearPassiveEffectsByType(SourceType.CARD);
        triggerManager.clearPassiveEffectsByType(SourceType.MONSTER);
        triggerManager.clearPassiveEffectsByType(SourceType.GLOBAL);
        deckManager.startBattleSetUp(); // 把所有牌放入deck 并洗牌
        player.setBlockRetention(false);
        player.resetAbility();
        player.getStatusManager().clearOneBattleBuffs();

        // triggers
        triggerManager.trigger(BATTLE_START, player, this::finishBattleIfNeeded);

        playerStartTurn();
    }

    public static GameEngine getInstance() {
        return INSTANCE;
    }

    // 玩家点 出牌
    public CardEffectResult playerUseCard(Card card) {
        if (state != PLAYER_TURN) {
            return CardEffectResult.failed("现在不是你的回合! ");
        }

        CardEffectResult result = DEFAULT_CHAIN.apply(card, player, monster);
        finishBattleIfNeeded();

        return result;
    }

    // 每回合开始
    private void playerStartTurn() {
        onSetAllBattleButtons(true);
        int needDraw = DeckManager.DRAW_MIN;
        needDraw = deckManager.draw(needDraw);

        if (needDraw == 0) {
            controller.setAllBattleActionButtonsEnabled(true);
            return;
        }

        drawingNow = true;
        controller.setAllBattleActionButtonsEnabled(false);
        controller.getCardAnimator().beginBatch(needDraw, () -> {
            drawingNow = false;
            Platform.runLater(() -> controller.setAllBattleActionButtonsEnabled(true));
        });

        if (!player.isBlockRetention()) {
            player.resetBlock();
        }
        player.refillEp();

        // 临时buff清除
        player.getStatusManager().clearOneTurnBuffs();
        player.getStatusManager().clearTempThorns();
        deckManager.clearCostOverrideThisTurn();
        // 加入遗物回合开始触发
        triggerManager.trigger(TURN_START, player, this::finishBattleIfNeeded);
        CardAnimator.resetSeq();

        roundCount++;
    }

    // 玩家点 结束回合
    public String endPlayerTurn() {
        if(state != PLAYER_TURN) return "";

        // Relic 或注册的 Triggers 触发
        triggerManager.trigger(TURN_END, player, this::finishBattleIfNeeded);
        // 玩家的 buff / debuff 触发

        player.getStatusManager().decayDebuffs(); // 虚弱 -1 回合
        deckManager.clearCostOverrideThisTurn();

        state = ENEMY_TURN;
        EventManager.fireEvent(new GameStateChangedEvent(ENEMY_TURN));
        return enemyAct();
    }

    // 怪物行动
    private String enemyAct() {
        StringBuilder sb = new StringBuilder();

        if (!monster.isBlockRetention()) {
            monster.resetBlock();
        }
        // AI 决定行动
        MonsterAction action = monster.getNextAction();
        sb.append(MonsterActionExecutor.executeMonsterAction(action, monster, player));
        // 回合结束 buff/debuff
        monster.getStatusManager().decayDebuffs();

        // 结束判定
        if (finishBattleIfNeeded()) {
            return sb.append(player.getHp() <= 0 ? "你失败了！\n" : "你成功了！\n").toString();
        }

        return sb.append(checkMayStarPlayerTurn()).toString();
    }

    private String checkMayStarPlayerTurn() {
        // 结束判定
        if (finishBattleIfNeeded()) {
            return player.getHp() <= 0 ? "你失败了！\n" : "你成功了！\n";
        }

        state = PLAYER_TURN;
        EventManager.fireEvent(new GameStateChangedEvent(PLAYER_TURN));

        playerStartTurn();

        return "进入下一回合! \n";
    }

    public boolean finishBattleIfNeeded() {
        // System.out.println("[DEBUG] HP=" + player.getHp() + ", MonsterHP=" + monster.getHp());
        if (player.getHp() <= 0 || monster.getHp() <= 0) {
            if (monster.getHp() <= 0) {
                triggerManager.trigger(ON_KILL, player, () -> {});
                rewardGoldKill();
            }
            state = GAME_OVER;
            EventManager.fireEvent(new GameStateChangedEvent(GAME_OVER));
            return true;
        }
        return false;
    }

    public void pauseGame() {
        if (state != PAUSED_ENGINE) {
            stateBeforePause = state;
        }
        state = PAUSED_ENGINE;
        EventManager.fireEvent(new GameStateChangedEvent(PAUSED_ENGINE));
    }

    public void resumeGame() {
        if (state == PAUSED_ENGINE) {
            state = Objects.requireNonNullElse(stateBeforePause, MAP_TURN_ENGINE);
            EventManager.fireEvent(new GameStateChangedEvent(stateBeforePause));
        }
    }

    public void resetEngineToRoleSelect() {
        roundCount = 1;
        monster = null;
        state = ROLE_SELECT_ENGINE;
    }

    public String getMonsterNextIntentDescription() {
        if (monster == null) return "[没有怪物]";
        MonsterAction nextAction = monster.peekNextAction();
        Map<String, Object> actionMap = nextAction.getActionMap();

        for (Map.Entry<String, Object> entry : actionMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "damage", "lifeSteal" -> {
                    int amount = 0;
                    if (value instanceof Number) {
                        amount = CombatCalculator.calculateFinalDamage(monster, player,
                                ((Number) value).intValue());
                    } else if (value instanceof Map) {
                        amount = CombatCalculator.calculateFinalDamage(monster, player,
                                ((Map<?, Number>) value).getOrDefault("value", 0).intValue());
                    }
                    return "准备攻击 " + amount;
                }
                case "gainBlock" -> {
                    return "准备获得护甲";
                }
                case "addBuff", "trigger" -> {
                    return "准备强化自身";
                }
                case "applyDebuff" -> {
                    return "准备施加负面效果";
                }
            }
        }
        return "";
    }

    // 加金币
    public void addGold(int amount) {
        if (player != null) {
            player.addGold(amount);
        }
    }

    public void rewardGoldKill() {
        int reward = 30 + new Random().nextInt(31);
        player.addGold(reward);
    }

    public void setBattleButtonsHandler(Consumer<Boolean> handler) {
        this.setAllBattleButtonsConsumer = handler;
    }
    public void onSetAllBattleButtons(Boolean enabled) {
        if (setAllBattleButtonsConsumer != null) {
            setAllBattleButtonsConsumer.accept(enabled);
        }
    }

    public CardActionExecutor getCardActionExecutor() {
        return cardActionExecutor;
    }
    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    // getters
    public Player getPlayer() {
        return player;
    }
    public Monster getMonster() {
        return monster;
    }
    public GameEngineState getState() {
        return this.state;
    }
    public int getRoundCount() {
        return roundCount;
    }
    public DeckManager getDeckManager() { return deckManager; }
    public GameController getController() { return controller; }

    // setters
    public void setState(GameEngineState state) {
        this.state = state;
    }

}
