package com.zixun.cardGame.controller;

import com.zixun.cardGame.animation.CardAnimator;
import com.zixun.cardGame.behavior.CardEffectResult;
import com.zixun.cardGame.manager.TriggerManager;
import com.zixun.cardGame.model.overlay.BaseOverlay;
import com.zixun.cardGame.model.overlay.InfoOverlay;
import com.zixun.cardGame.model.overlay.PileOverlay;
import com.zixun.cardGame.observer.EventListener;
import com.zixun.cardGame.observer.EventManager;
import com.zixun.cardGame.event.GameStateChangedEvent;
import com.zixun.cardGame.manager.GameEngine;
import com.zixun.cardGame.observer.Observer;
import com.zixun.cardGame.manager.DeckManager;
import com.zixun.cardGame.manager.GameManager;
import com.zixun.cardGame.map.LayeredGameMap;
import com.zixun.cardGame.map.MapNode;
import com.zixun.cardGame.type.CardViewSize;
import com.zixun.cardGame.type.TargetType;
import com.zixun.cardGame.util.*;
import com.zixun.cardGame.model.card.Card;
import com.zixun.cardGame.model.character.Monster;
import com.zixun.cardGame.model.character.Player;

import com.zixun.cardGame.model.relic.Relic;
import com.zixun.cardGame.type.GameControllerState;
import com.zixun.cardGame.type.GameEngineState;
import com.zixun.cardGame.model.overlay.PauseOverlay;
import com.zixun.cardGame.view.BuffRenderer;
import com.zixun.cardGame.view.CardView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.shape.Polygon;
import javafx.scene.effect.DropShadow;
import java.io.IOException;
import java.util.Deque;
import java.util.ArrayDeque;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

import java.util.*;
import java.util.List;

import static com.zixun.cardGame.type.GameEngineState.*;
import static com.zixun.cardGame.type.GameControllerState.*;
import static com.zixun.cardGame.type.NodeTypeEnum.*;

public class GameController {

    @FXML private StackPane rootPane;
    @FXML private VBox mainMenuPane;
    @FXML private BorderPane gameContent;
    @FXML private AnchorPane battleLogPane;

    // Select
    @FXML private BorderPane roleSelectPane;
    @FXML private VBox classBtnBar, infoBar;
    @FXML private Button btnWarrior, btnMage, btnRogue;

    // Shared Top
    @FXML private HBox Top;
    @FXML private Label playerHpLabelTop;
    @FXML private Label goldLabel;
    @FXML private Label floorLabel;
    @FXML private Label levelLabel;
    @FXML private Label cardHoldTop;
    @FXML private HBox relicBar;
    @FXML private Button openInventoryButton;

    // CenterMap
    @FXML private AnchorPane mapPane;
    @FXML private ScrollPane mapScroll;
    @FXML private StackPane layerStack;
    @FXML private GridPane mapGrid;
    @FXML private Pane pathLayer;
    @FXML private Label cardInExhaustLabel;

    // CenterBattle
    @FXML private AnchorPane battlePane;
    @FXML private ProgressBar playerHpBar;
    @FXML private Label playerBlockLabel;
    @FXML private Label playerHpValueLabel;
    @FXML private FlowPane playerBuffBar;
    @FXML private FlowPane monsterBuffBar;
    @FXML private Label monsterIntentLabel;
    @FXML private ImageView playerImageView;
    @FXML private ImageView monsterImageView;
    @FXML private ProgressBar monsterHpBar;
    @FXML private Label monsterHpValueLabel;
    @FXML private Label monsterBlockLabel;
    @FXML  private Label battleRoundLabel;
    @FXML private Label battleMonsterNameLabel;
    @FXML private ListView<String> gameLog;
    @FXML private Region centerArea;
    @FXML private VBox playerArea;
    @FXML private VBox monsterArea;


    // BottomBattle
    @FXML private Label cardInDeckLabel;
    @FXML private Label cardInDiscardLabel;
    @FXML private HBox handCardBox;
    @FXML private Label epLabel;
    @FXML private Button endTurnBtn;

    // 弃用
    @FXML private Button useBtn;
    @FXML private Button discardBtn;
    @FXML private Button drawBtn;

    private static final GameManager gameManager = GameManager.getInstance();
    private final GameEngine engine = GameEngine.getInstance();
    private final TriggerManager triggerManager = TriggerManager.getInstance();
    private final DeckManager deckManager = new DeckManager();
    private GameControllerState currentMode;
    private Player player;

    private Observer<Player> playerObserver;
    private Observer<Monster> monsterObserver;
    private EventListener<GameStateChangedEvent> gameStateChangedEventEventListener;
    private final Deque<Node> overlayStack = new ArrayDeque<>();

    private static boolean isFirstTime = true;
    private boolean gameOverHandled = false;
    private boolean rendering = false;
    private boolean isESCMenuOpen = false;
    private PauseOverlay pauseOverlay;

    // 放抖动
    private long lastEscPressTime = 0;
    private static final long ESC_DEBOUNCE_MS = 200;
    // UI
    public static final int CELL = 50;
    public static final int GAP = 30;

    private CardAnimator cardAnimator;
    // 卡牌拖拽
    private CardView draggingCard;
    private Card cardBeingDragged;
    private double offsetX, offsetY;
    private boolean isAiming = false;

    private final Line arrow = new Line();
    private static final double ARROW_THICKNESS = 6;
    private static final double ARROW_HEAD_H    = 36;

    private boolean arrowReady = false;
    private Point2D currentMousePosition = new Point2D(0, 0);
    private Point2D returnPos;
    private Pane dragOriginParent;
    private int  dragOriginIndex;
    private final Polygon  arrowHead = new Polygon();
    private enum DragPhase { NONE, DRAGGING, FLYING }
    private DragPhase dragPhase = DragPhase.NONE;
    private final Map<MapNode, Button> nodeToButtonMap = new HashMap<>();


    // 切换模式
    public void switchToMode(GameControllerState mode) {
        if (mode == ROLE_SELECT_CONTROLLER) {
            closeAllOverlays();
        }
        currentMode = mode;
        mainMenuPane.setVisible(mode == MAIN_MENU_CONTROLLER);
        roleSelectPane.setVisible(mode == ROLE_SELECT_CONTROLLER);
        mapPane.setVisible(mode == MAP_TURN_CONTROLLER);
        gameContent.setVisible(mode == MAP_TURN_CONTROLLER || mode == BATTLE_CONTROLLER);
        battleLogPane.setVisible(mode == MAP_TURN_CONTROLLER || mode == BATTLE_CONTROLLER);
        battlePane.setVisible(mode == BATTLE_CONTROLLER);
    }

    @FXML
    private void initialize() {
        System.out.println("initializing");
        // ESC功能
        rootPane.sceneProperty().addListener((
                obs, o, scene) -> {
            if (scene != null) {
                registerGlobalEscHandler(scene);
            }
        });
        // GameStatus 状态变化监听
        EventManager.addListener(GameStateChangedEvent.class, event -> {
            if (event.getNewState() == GAME_OVER) {
                Platform.runLater(this::checkIfGameOver);
            }
            updateUI();
        });
        engine.setBattleButtonsHandler(this::setAllBattleActionButtonsEnabled);

        switchToMode(MAIN_MENU_CONTROLLER);

        infoBar.widthProperty().addListener((obs, o, w) -> syncWidths(w.doubleValue() / 3));
        classBtnBar.widthProperty().addListener((obs, o, w) -> syncWidths(w.doubleValue() / 3));

        // UI
        mapGrid.setAlignment(Pos.TOP_CENTER);
        mapGrid.setHgap(GAP);
        mapGrid.setVgap(GAP);
        initArrow();

        // Log 自定义
        gameLog.setCellFactory(lv -> new ListCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.setMaxWidth(200);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    // 🌟 判断内容前缀决定颜色
                    if (item.startsWith("[MONSTER]")) {
                        label.setTextFill(Color.CRIMSON);
                    } else if (item.startsWith("[PLAYER]")) {
                        label.setTextFill(Color.ROYALBLUE);
                    } else {
                        label.setTextFill(Color.BLACK); // 默认颜色
                    }

                    label.setText(item
                            .replace("[MONSTER]","")
                            .replace("[PLAYER]", "")); // 去掉全部
                    setGraphic(label);
                }
            }
        });
        // Log 滚动 让 ListView 先准备好滚动条
        Platform.runLater(() -> {
            ScrollBar vBar = (ScrollBar) gameLog.lookup(".scroll-bar:vertical");
            if (vBar != null) vBar.setUnitIncrement(8);   // 可选：调滚动灵敏度
        });
        // Scene 创建后再注册过滤器
        rootPane.sceneProperty().addListener((obs, o, scene) -> {
            if (scene == null) return;

            scene.addEventFilter(ScrollEvent.SCROLL, e -> {

                if (!battleLogPane.isVisible()) return;   // 日志隐藏时忽略

                // 把 battleLogPane 的局部坐标转成 Scene 坐标
                Bounds logBounds = battleLogPane.localToScene(
                        battleLogPane.getLayoutBounds());

                boolean inside = logBounds.contains(e.getSceneX(), e.getSceneY());
                if (!inside) return;                      // 鼠标不在日志 → 让地图滚

                // ---------- 手动滚日志 ----------
                // 方法 A：直接改滚动条
                ScrollBar vBar = (ScrollBar) gameLog.lookup(".scroll-bar:vertical");
                if (vBar != null) {
                    double step = vBar.getUnitIncrement();        // 每格增量≈一行
                    double delta = Math.signum(e.getDeltaY()) * step;
                    vBar.setValue(clamp(vBar.getValue() - delta,
                            vBar.getMin(), vBar.getMax()));
                }
                // 方法 B（更简单但跳跃感大）：按索引滚
                // int lines = e.getDeltaY() > 0 ? -3 : 3;
                // gameLog.scrollTo(gameLog.getSelectionModel().getSelectedIndex() + lines);

                e.consume();   // 阻止事件再冒泡到地图
            });
        });

        // pathLayer 绑定宽高（但不绑 layoutX/Y）
        pathLayer.prefWidthProperty().bind(layerStack.widthProperty());
        pathLayer.prefHeightProperty().bind(layerStack.heightProperty());

        Log.bind(gameLog);

        // 牌堆显示
        cardInDeckLabel.setOnMouseClicked(e ->
                OverlayUtil.openOverlay(rootPane, overlayStack,
                        () -> new PileOverlay("抽牌堆",
                                deckManager.getDeckPile(),
                                player,
                                rootPane,
                                overlayStack)));

        cardInDiscardLabel.setOnMouseClicked(e ->
                OverlayUtil.openOverlay(rootPane, overlayStack,
                        () -> new PileOverlay("弃牌堆",
                                deckManager.getDiscardPile(),
                                player,
                                rootPane,
                                overlayStack)));

        cardInExhaustLabel.setOnMouseClicked(e ->
                OverlayUtil.openOverlay(rootPane, overlayStack,
                        () -> new PileOverlay("消失牌堆",
                                deckManager.getExhaustPile(),
                                player,
                                rootPane,
                                overlayStack)));

        // Animator
        cardAnimator = new CardAnimator(rootPane, handCardBox, cardInDeckLabel, cardInDiscardLabel,
                this::createInteractiveCardView,
                this::renderHand);

        deckManager.addDrawListener(card -> { // 注册监听
            cardAnimator.flyDraw(card);
        });
        gameOverHandled = false;
    }
    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    public void initWithPlayerAndDeck(Player player) {
        this.player = player;
        engine.resetEngineToRoleSelect();

        GameManager.getInstance().resetToLayeredGameMap();
        startNewLevel();

        centerScroll();
        updateUI();
    }

    private void startNewLevel() {
        if (isFirstTime) {
            isFirstTime = false;
        } else {
            gameManager.moveToNextLevel();
        }

        // 取消旧怪物的 Observer
        if (engine.getMonster() != null && monsterObserver != null) {
            engine.getMonster().removeObserver(monsterObserver);
        }

        // 玩家位置刷新
        LayeredGameMap currentMap = gameManager.getCurrentLevel().getLayeredGameMap();
        currentMap.resetPlayerToStart();
        MapNode start = currentMap.getStartNode();

        currentMap.moveTo(start);
        if (gameManager.getCurrentLevelNumber() == 1 && !start.isVisited()) {
            start.setVisited(true);
            onEnterNode(start);
        } else {
            start.setVisited(true);
        }

        // 设置初始状态在 MAP_TURN, 刷新 shop, 回到地图
        engine.setState(GameEngineState.MAP_TURN_ENGINE);
        switchToMode(getUIState(engine.getState()));

        Log.clear();
        Log.write("进入第 " + gameManager.getCurrentLevelNumber() + " 层迷宫!");

        centerScroll();

        updateUI();
    }

    public void updateUI() {
        GameEngineState state = engine.getState();
        GameControllerState UIState = getUIState(state);
        if (UIState != currentMode) {
            switchToMode(UIState); // 只有状态发生变化才切换
        }

        switch (UIState) {
            case ROLE_SELECT_CONTROLLER -> {
            }
            case MAP_TURN_CONTROLLER -> {
                updateMapStatusBar();
                renderMap(gameManager.getCurrentLevel().getLayeredGameMap());
            }
            case BATTLE_CONTROLLER -> {
                updateBattleInfo();
                updateMapStatusBar();
            }
        }
        renderRelics();
    }

    public void renderHand() {
        handCardBox.getChildren().clear();
        for (Card card : deckManager.getHand()) {
            handCardBox.getChildren().add(createInteractiveCardView(card));
        }
    }

    public void renderMap(LayeredGameMap layeredGameMap) {
        if (rendering) return;
        rendering = true;

        mapGrid.getChildren().clear();
        pathLayer.getChildren().clear();
        int totalRows = layeredGameMap.getMapRows().size();
        MapNode startNode = layeredGameMap.getStartNode();
        MapNode playerNode = layeredGameMap.getPlayerNode();
        Button playerButton = null;

        // 添加地图按钮 Buttons
        for (List<MapNode> rowNodes : layeredGameMap.getMapRows()) {
            for (MapNode node : rowNodes) {
                int uiRow = totalRows - 1 - node.row;
                int uiCol = layeredGameMap.getUiCol(node);

                // 创建按钮
                Button button = new Button(node.content.getName());
                button.setPrefSize(CELL, CELL);
                button.setShape(new javafx.scene.shape.Circle(CELL / 2.0)); // 圆形
                String fill = switch (node.content) {          // import static com.zixun.cardGame.type.NodeContentEnum.*;
                    case ENEMY      -> "#ffcccc";   // 浅红
                    case MINI_BOSS      -> "#d5a6ff";   // 紫
                    case FINAL_BOSS       -> "#ff9b00";   // 橙
                    case TREASURE      -> "#ffd700";   // 金
                    case EVENT      -> "#fff799";   // 黄
                    case REST       -> "#99ccff";   // 蓝
                    case SHOP       -> "#9acd32";   // 绿
                    default         -> "#ffffff";   // 其他 白
                };
                String baseStyle = String.format("""
                            -fx-background-color: %s;
                            -fx-background-radius: 50%%;
                            -fx-font-weight: bold;
                            -fx-font-size: 10;
                        """, fill);
                button.setStyle(baseStyle);

                // 玩家格 特殊化
                if (node == startNode) {
                    Button dummy = new Button();
                    dummy.setPrefSize(CELL, CELL);      // 仍给正常尺寸，ScrollPane 才能正确计算高度
                    dummy.setVisible(false);            // 完全不可见，但仍参与布局
                    GridPane.setRowIndex(dummy, uiRow);
                    GridPane.setColumnIndex(dummy, uiCol);
                    mapGrid.getChildren().add(dummy);
                    nodeToButtonMap.put(node, dummy);   // 供连线或滚动时取坐标
                    continue;
                } else if (node == playerNode) {
                    button.setStyle("-fx-border-color: blue; -fx-border-width: 3px;");
                    playerButton = button;
                } else if (layeredGameMap.canMove(playerNode, node)) {
                    button.setOnAction(e -> {
                        layeredGameMap.moveTo(node);
                        renderMap(layeredGameMap);
                        onEnterNode(node);
                    });
                    button.setStyle(button.getStyle() + """
                                ; -fx-border-color: #4caf50;
                                -fx-border-width: 2px;
                                -fx-background-color: #f0fff0;
                            """);
                } else {
                    button.setDisable(true);
                    button.setMouseTransparent(true); // 不响应点击
                    button.setFocusTraversable(false); // 不能被 Tab 键选中
                    button.setStyle(button.getStyle() + "; -fx-opacity: 1;"); // 恢复不透明（取消灰掉）
                }

                // 倒排
                GridPane.setRowIndex(button, uiRow);
                GridPane.setColumnIndex(button, uiCol);
                mapGrid.getChildren().add(button);
                nodeToButtonMap.put(node, button);
            }
        }
        mapGrid.applyCss();
        mapGrid.layout();
        Platform.runLater(() -> {
            // Lines
            for (List<MapNode> rowNodes : layeredGameMap.getMapRows()) {
                for (MapNode node : rowNodes) {
                    if (node == startNode) continue;
                    int uiRow = totalRows - 1 - node.row;
                    int colA = layeredGameMap.getUiCol(node);

                    int nextRowIdx = node.row + 1;
                    if (nextRowIdx >= layeredGameMap.getMapRows().size()) continue;
                    for (MapNode down : layeredGameMap.getMapRows().get(nextRowIdx)) {
                        if (down == startNode) continue;
                        if (!layeredGameMap.canMove(node, down)) continue;
                        int colB = layeredGameMap.getUiCol(down);

                        Button btnA = getButtonAt(mapGrid, uiRow, colA);
                        Button btnB = getButtonAt(mapGrid, uiRow - 1, colB);
                        if (btnA == null || btnB == null) continue;

                        Point2D p1 = pathLayer.sceneToLocal(
                                btnA.localToScene(btnA.getWidth() / 2, btnA.getHeight() / 2).add(0, 6));
                        Point2D p2 = pathLayer.sceneToLocal(
                                btnB.localToScene(btnB.getWidth() / 2, btnB.getHeight() / 2).add(0, -6));
                        Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                        line.setStrokeWidth(2.5);
                        line.setStroke(Color.web("#9d744a"));
                        line.setStrokeLineCap(StrokeLineCap.ROUND);
                        pathLayer.getChildren().add(line);
                    }
                }
            }
        });
        centerScroll();
        rendering = false;
    }

    private Button getButtonAt(GridPane grid, int row, int col) {
        for (Node node : grid.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            if (r != null && c != null && r == row && c == col && node instanceof Button btn) return btn;
        }
        return null;
    }

    public void renderRelics() {
        relicBar.getChildren().clear();
        List<Relic> relicList = player.getRelicList();
        for (Relic relic : relicList) {
            Label label = new Label(relic.getName());
            label.setStyle("-fx-background-color: lightgoldenrodyellow; -fx-padding: 5 10 5 10; -fx-border-color: darkgoldenrod;");

            PopupHelper.bind(label, relic.getDescription());
            relicBar.getChildren().add(label);
        }
    }


    public void renderBattleRelics() {
        relicBar.getChildren().clear();
        List<Relic> relicList = player.getRelicList();
        for (Relic relic : relicList) {
            Label label = new Label(relic.getName());
            label.setStyle("-fx-background-color: lightgoldenrodyellow; -fx-padding: 5 10 5 10; -fx-border-color: darkgoldenrod;");
            PopupHelper.bind(label, relic.getDescription());

            relicBar.getChildren().add(label);
        }
        goldLabel.setText("金币: " + player.getGold());
    }

    private void updateMapStatusBar() {
        playerHpLabelTop.setText("HP: " + player.getHp() + " / " + player.getMaxHp());
        goldLabel.setText("金币: " + player.getGold());
        levelLabel.setText("第 " + gameManager.getCurrentLevelNumber() + " 层 / 共 " + gameManager.getLevelsSize() + " 层");
        cardHoldTop.setText("卡牌持有: " + deckManager.getAllCards().size() + " 持有上限: " + DeckManager.MAX_DECK);
    }

    public void updateBattleInfo() {
        updateBattlePlayerInfo();
        updateBattleMonsterInfo();
        BuffRenderer.renderAll(player, playerBuffBar);
        BuffRenderer.renderAll(engine.getMonster(), monsterBuffBar);
        epLabel.setText("EP： " + getEpBar());
        cardInDeckLabel.setText("抽牌池: " + deckManager.getDeckSize() + " ");
        cardInDiscardLabel.setText(" 弃牌池: " + deckManager.getDiscardSize() + " ");
        refreshExhaustCount();
    }

    public String getEpBar() {
        return " " + player.getEp() + " / " + player.getMaxEp();
    }

    public void updateBattlePlayerInfo() {
        int playerCurrentHp = player.getHp();
        if (playerCurrentHp > 0) {
            playerHpBar.setProgress(player.getHp() * 1.0 / player.getMaxHp());
            playerHpValueLabel.setText(player.getHp() + " / " + player.getMaxHp());
        } else {
            playerHpBar.setProgress(0 * 1.0 / player.getMaxHp());
            playerHpValueLabel.setText("0 / " + player.getMaxHp());
        }
        playerBlockLabel.setText("护甲: " + player.getBlock());
    }

    public void updateBattleMonsterInfo() {
        Monster monster = engine.getMonster();
        battleMonsterNameLabel.setText(engine.getMonster().getName());
        if (monster.getHp() > 0) {
            monsterHpBar.setProgress(monster.getHp() * 1.0 / monster.getMaxHp());
            monsterHpValueLabel.setText(monster.getHp() + " / " + monster.getMaxHp());
        } else {
            monsterHpBar.setProgress(0 * 1.0 / monster.getMaxHp());
            monsterHpValueLabel.setText("0 / " + monster.getMaxHp());
        }
        monsterBlockLabel.setText("护甲: " + monster.getBlock());

        if (engine.getState() == PLAYER_TURN) {
            monsterIntentLabel.setText(engine.getMonsterNextIntentDescription());
        } else {
            monsterIntentLabel.setText("");
        }
    }

    private void onEnterNode(MapNode node) {
        gameOverHandled = false;
        NodeEventProcessor.processNode(this, node);
    }

    public void startBattle(Monster monster) {

        // 保存进入战斗的格子位置
        gameManager.setBattleStartNode(gameManager.getCurrentLevel().getLayeredGameMap().getPlayerNode());

        // 删除旧的 Observer
        if (engine.getMonster() != null && monsterObserver != null) {
            engine.getMonster().removeObserver(monsterObserver);
        }
        // 创建玩家，怪物，卡牌, PLAYER_TURN
        engine.initialize(player, monster, gameManager.getCurrentLevelNumber(), deckManager, this);

        // 注册玩家 Observer
        playerObserver = p -> updateBattlePlayerInfo();
        player.addObserver(playerObserver);

        // 注册怪物 Observer
        monsterObserver = m -> updateBattleMonsterInfo();
        engine.getMonster().addObserver(monsterObserver);

        // 换场景 默认 PLAYER_TURN -> 映射成UI的BATTLE_CONTROLLER
        switchToMode(getUIState(engine.getState()));

        updateUI();
        renderBattleRelics();
    }

    private void checkIfGameOver() {

        if (gameOverHandled || engine.getState() != GAME_OVER) { return; }
        gameOverHandled = true;

        if (player.getHp() <= 0) {
            Map<String, Runnable> buttonMap = new LinkedHashMap<>();
            buttonMap.put("重新开始", this::doRestartClick);
            buttonMap.put("返回主菜单", this::backToMainMenu);
            buttonMap.put("退出游戏", this::exitGame);
            showInfoDialog("可惜", "你死了！游戏结束！", buttonMap);
        } else if (gameManager.getBattleStartNode().type == BOSS) {
            if (gameManager.hasMoreLevel()) {
                showInfoDialog("你一阵翻找", "发现了一个通道", Map.of(
                        "前往下一层...", this::doNextLevel
                ));
                BattleRewardProcessor.showRandomRewardLevelBoss(this);
            } else {
                Map<String, Runnable> buttonMap = new LinkedHashMap<>();
                buttonMap.put("重新开始", this::doRestartClick);
                buttonMap.put("返回主菜单", this::backToMainMenu);
                buttonMap.put("退出游戏", this::exitGame);
                showInfoDialog("胜利", "你击败了Boss！恭喜通关所有关卡！游戏结束!", buttonMap);
            }
        } else {
            // content == ENEMY || content == MINI_BOSS
            BattleRewardProcessor.showRandomRewardEnemy(this);
        }

        centerScroll();
        deckManager.endBattleSetUp();
        renderHand();
        // 注销 Observers
        destroyBattleObservers();
    }

    public boolean isNeighbor(int playerX, int playerY, int x, int y) {
        return (Math.abs(playerX - x) + Math.abs(playerY - y)) == 1;
    }


    private void applySelectEffect(Button btn, VBox wrapper, boolean isSelected) {
        if (isSelected) {
            btn.setEffect(new DropShadow(10, Color.DARKBLUE));
            wrapper.setTranslateY(-10);
        } else {
            btn.setEffect(null);
            wrapper.setTranslateY(0);
        }
    }

    public void setAllBattleActionButtonsEnabled(boolean enabled) {
        endTurnBtn.setDisable(!enabled);
    }

    private void setAllBattleActionButtonsVisible(boolean enabled) {
        endTurnBtn.setVisible(enabled);
    }

    private void setAllMapActionEnabled(boolean enabled) {
        mapGrid.setDisable(!enabled);
        openInventoryButton.setDisable(!enabled);
    }

    @FXML
    private void onEndTurnClick() {
        if (engine.drawingNow || engine.getState() != PLAYER_TURN) return;

        setAllBattleActionButtonsEnabled(false);

        /* 先做动画：UI 逐张飞弃牌堆 */
        cardAnimator.animateDiscardHand(
                handCardBox,
                // 每飞完一张，逻辑层弃牌
                cv -> deckManager.discardCardInHand(cv.getCard()),
                () -> {                               // 所有动画结束后 → 进入怪物回合
                    renderHand();                     // 清空手牌 UI（保险）
                    // 触发怪物回合, 扣血会触发 updateUI() 在 EventListener里
                    String log = engine.endPlayerTurn();
                    Log.write(log);
                    updateUI();                       // 刷 EP/HP 等
                });
    }

    @FXML
    private void onRestartClick(ActionEvent event) {
        doRestartClick();
    }

    private void doRestartClick() {
        resetForNewGame();
        // 回到选人界面
        switchToMode(getUIState(engine.getState()));
    }
    public void backToMainMenu() {
        closeAllOverlays();
        resetForNewGame();
        switchToMode(MAIN_MENU_CONTROLLER);
    }

    public void resetForNewGame() {
        CardAnimator.resetSeq();
        destroyBattleObservers();
        engine.resetEngineToRoleSelect();
        player.getStatusManager().clearAllBuffs();
        triggerManager.clearAll();
        GameController.isFirstTime = true;
        deckManager.reset();
        clearHandUI();
        updateUI();
    }

    private void destroyBattleObservers() {
        if (playerObserver != null) {
            player.removeObserver(playerObserver);
            playerObserver = null;
        }
        if (engine.getMonster() != null && monsterObserver != null) {
            engine.getMonster().removeObserver(monsterObserver);
            monsterObserver = null;
        }
    }

    @FXML
    private void onBackToMap() {
        doBackToMap();
    }

    public void doBackToMap() {
        engine.setState(MAP_TURN_ENGINE);
        destroyBattleObservers();
        // 回到地图
        switchToMode(MAP_TURN_CONTROLLER);

        updateUI();
    }

    @FXML
    private void onNextLevel(ActionEvent actionEvent) {
        doNextLevel();
    }

    private void doNextLevel() {
        if (engine.getState() == PAUSED_ENGINE) return;
        startNewLevel();
        centerScroll();
    }

    public void showInfoDialog(String title, String message, Map<String, Runnable> options) {
        OverlayUtil.openOverlay(rootPane, overlayStack,
                () -> new InfoOverlay(title, message, options));
    }

    private void registerGlobalEscHandler(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                toggleESCMenu();
                event.consume();
            }
        });
    }

    @FXML
    private void onNewGame() {
        mainMenuPane.setVisible(false); // 隐藏主菜单
        switchToMode(ROLE_SELECT_CONTROLLER); // 进入选人界面
    }

    @FXML
    private void onSettings() {
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> {
            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                    new Label("设置界面（暂未开放）"),
                    new Button("关闭设置")
            );
            BaseOverlay overlay = new BaseOverlay(300, 200);
            overlay.getChildren().add(content);
            ((Button) content.getChildren().get(1)).setOnAction(e -> overlay.closeOverlay());
            return overlay;
        });
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    private void toggleESCMenu() {
        long now = System.currentTimeMillis();
        if (now - lastEscPressTime < ESC_DEBOUNCE_MS) return; // 忽略过快按下
        lastEscPressTime = now;

        if (isESCMenuOpen) {
            isESCMenuOpen = false;
            closePauseOverlay();
        } else {
            isESCMenuOpen = true;
            openPauseOverlay();
        }
    }

    private void openPauseOverlay() {
        pauseOverlay = OverlayUtil.openOverlay(rootPane, overlayStack, () -> new PauseOverlay(
                this::closePauseOverlay, // 继续游戏
                this::doRestartClick,    // 重新开始
                this::backToMainMenu,
                this::exitGame));
        engine.pauseGame();
    }

    private void closePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.closeOverlay();   // 触发先前 setOnClose 回调，dim+窗一起移除
            pauseOverlay = null;
        }
        engine.resumeGame();
    }

    // 关闭所有浮窗, 背包, 半透明遮罩
    private void closeAllOverlays() {
        while (!overlayStack.isEmpty()) {
            rootPane.getChildren().remove(overlayStack.pop());
        }
        pauseOverlay = null;
    }

    private void exitGame() {
        Platform.runLater(Platform::exit);
    }

    public Player getPlayer() {
        return this.player;
    }

    @FXML
    private void onOpenInventory() {
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/zixun/cardGame/view/full-inventory.fxml"));
            Parent inventoryRoot = null;
            try {
                inventoryRoot = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            FullInventoryController controller = loader.getController();
            controller.initialize(player, deckManager.getAllCards(), player.getRelicList(), rootPane, overlayStack);

            BaseOverlay overlay = new BaseOverlay(600, 400);
            overlay.getChildren().add(inventoryRoot);
            controller.setOnCloseRequest(overlay::closeOverlay);
            return overlay;
        });
    }

    public void openShop() {
        OverlayUtil.openOverlay(rootPane, overlayStack, () -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/zixun/cardGame/view/shop-overlay.fxml"));
            Parent shopRoot = null;
            try {
                shopRoot = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ShopOverlayController controller = loader.getController();
            controller.initData(player, deckManager, rootPane, overlayStack);

            BaseOverlay overlay = new BaseOverlay(600, 400);
            overlay.getChildren().add(shopRoot);
            controller.setOnCloseRequest(() -> {
                overlay.closeOverlay();
                renderRelics();
            });
            controller.setOnUpdateUI(this::updateUI);

            return overlay;
        });
    }

    private GameControllerState getUIState(GameEngineState state) {
        return switch (state) {
            case ROLE_SELECT_ENGINE -> (currentMode == MAIN_MENU_CONTROLLER ?
                    MAIN_MENU_CONTROLLER : ROLE_SELECT_CONTROLLER);
            case MAP_TURN_ENGINE -> MAP_TURN_CONTROLLER;
            case PLAYER_TURN, ENEMY_TURN -> BATTLE_CONTROLLER;
            case GAME_OVER, PAUSED_ENGINE -> currentMode; // 停留在当前页面
        };
    }


    // 鼠标事件
    private CardView createInteractiveCardView(Card card) {
        CardView view = CardView.forCombat(card, player, CardViewSize.BATTLE);
        view.setHoverEffectEnabled(true);

        view.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) beginCardDrag(view, e);
        });
        view.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) cancelCardDrag();
        });
        GameEngine engine = GameEngine.getInstance();
        boolean canOperate =
                engine.getState() == PLAYER_TURN &&
                        engine.getState() != PAUSED_ENGINE &&
                        engine.getState() != MAP_TURN_ENGINE;
        view.setDisable(!canOperate);
        return view;
    }

    public void beginCardDrag(CardView view, MouseEvent e) {
        if (dragPhase != DragPhase.NONE) return;   // 任何动画中都不再接受拖拽
        dragPhase = DragPhase.DRAGGING;

        if (draggingCard != null) return;
        if (!arrowReady) {            // 懒初始化
            initArrow();
            rootPane.getChildren().addAll(arrow, arrowHead);
            arrowReady = true;
        }

        draggingCard      = view;
        cardBeingDragged  = view.getCard();
        dragOriginParent  = (Pane) view.getParent();
        dragOriginIndex   = dragOriginParent.getChildren().indexOf(view);
        offsetX = offsetY = 0;  //  卡牌中心永远对准鼠标
        //记下拖起前卡牌左上角在 rootPane 坐标，用于回位动画
        Bounds bScene = view.localToScene(view.getBoundsInLocal());
        returnPos = rootPane.sceneToLocal(bScene.getMinX(), bScene.getMinY());

        // 提升层级
        dragOriginParent.getChildren().remove(view);
        view.setManaged(false);
        rootPane.getChildren().add(view);

        //  立即把卡牌放到鼠标所在位置
        placeCardAt(e.getSceneX() - offsetX, e.getSceneY() - offsetY);

        // 全局监听
        Scene sc = rootPane.getScene();
        sc.addEventFilter(MouseEvent.MOUSE_MOVED,  this::updateCardDrag);
        sc.addEventFilter(MouseEvent.MOUSE_PRESSED,this::globalMousePress); //  右键兜底
    }
    private void globalMousePress(MouseEvent e) {
        if (dragPhase != DragPhase.DRAGGING) return;

        if (cardBeingDragged == null) {
            e.consume();
            return;
        }

        /* 1) 右键 ⇒ 直接取消 */
        if (e.getButton() == MouseButton.SECONDARY) {
            cancelCardDrag();
            e.consume();
            return;
        }

        /* 2) 左键 ⇒ 判定区域 & 卡牌目标类型 */
        if (e.getButton() != MouseButton.PRIMARY) return;

        Bounds handB    = handCardBox.localToScene(handCardBox.getBoundsInLocal());
        boolean inHand  = handB.contains(e.getSceneX(), e.getSceneY());

        if (inHand) {                      // ← 直接撤销
            finishCardDrag(true, null);
            e.consume();
            return;
        }

        TargetType type = cardBeingDragged.getTargetType();
        Bounds monsterB = monsterArea.localToScene(monsterArea.getBoundsInLocal());
        boolean inMonster = monsterB.contains(e.getSceneX(), e.getSceneY());

        /* 判定能否立即使用 */
        if (type == TargetType.NONE) {
            tryUseDraggedCard(null);         // 无目标型
            e.consume();
        } else if (type == TargetType.MONSTER && inMonster) {
            tryUseDraggedCard(engine.getMonster());   // 只有一个怪时直接引用
            e.consume();
        }
        // 其余情况：保持拖拽，不做任何处理
    }
    private void tryUseDraggedCard(Monster target) {
        Card played = cardBeingDragged;
        CardView cardNode = draggingCard;            // 拿在手上的节点
        cardNode.setHoverEffectEnabled(true);

        CardEffectResult result = engine.playerUseCard(played);  // 使用卡牌
        boolean success = result.success();
        if (!success) {                 //  EP 不够 / 目标非法
            finishCardDrag(true, null);
            return;
        }

        updateBattleInfo(); // 扣 EP

        // 数据处理 UI刷新
        deckManager.tryToDiscard(played);
        updateUI();
        renderHand();

        // 释放拖拽锁 允许动画中操作其他卡牌
        clearDragState();

        boolean vanish = played.getExhaustAfterUse() || "能力".equals(played.getType());
        if (vanish) {
            // 能力牌 消失牌 -> 淡出动画
            cardAnimator.fadeAndRemove(cardNode, this::checkIfGameOver);
        } else {
            // 普通牌 -> 飞向弃牌堆
            cardAnimator.flyToDiscard(cardNode, this::checkIfGameOver);
        }
    }

    private void clearDragState() {
        hideArrow();
        dragPhase = DragPhase.NONE;
        draggingCard = null;
        cardBeingDragged = null;
    }

    public void updateCardDrag(MouseEvent e) {
        if (dragPhase != DragPhase.DRAGGING || cardBeingDragged == null) return;
        currentMousePosition = new Point2D(e.getSceneX(), e.getSceneY());

        TargetType type = cardBeingDragged.getTargetType();   // NONE / MONSTER
        Bounds hand = handCardBox.localToScene(handCardBox.getBoundsInLocal());
        Bounds monsterB = monsterArea.localToScene(monsterArea.getBoundsInLocal());

        boolean mouseInsideHand = hand.contains(currentMousePosition);
        boolean mouseOverMonster = monsterB.contains(currentMousePosition);

        if (type == TargetType.NONE || mouseInsideHand) {
            // 普通跟随
            draggingCard.setHoverEffectEnabled(true);
            placeCardAt(e.getSceneX() - offsetX, e.getSceneY() - offsetY);
            hideArrow();
        } else { // 只能打怪 鼠标出了手牌区
            draggingCard.setHoverEffectEnabled(false);
            snapCardToHandCenter(hand);
            showArrowToMouse(e);
            Color col = (type == TargetType.MONSTER && mouseOverMonster) ?
                        Color.web("#ff4d4f") : Color.web("#888888");
            arrow.setStroke(col);
            arrowHead.setFill(col);

            // 更新手牌伤害显示 附加 VULNERABLE 数值
            if (draggingCard != null && draggingCard instanceof CardView) {
                Monster hoverTarget = mouseOverMonster ? engine.getMonster() : null;
                draggingCard.setHoverTarget(hoverTarget);
                draggingCard.updateDescription();           // 重新渲染 {damage} {block}
            }
        }
    }

    private void placeCardAt(double sceneX, double sceneY) {
        Point2D p = rootPane.sceneToLocal(sceneX, sceneY);
        draggingCard.relocate(p.getX() - draggingCard.getWidth() / 2,
                p.getY() - draggingCard.getHeight() / 2);
    }

    private void snapCardToHandCenter(Bounds hand) {
        if (!isAiming) {     // 第一次进入瞄准状态时才把卡牌归位
            Point2D centre = rootPane.sceneToLocal(
                    hand.getMinX() + hand.getWidth() / 2,
                    hand.getMinY() + hand.getHeight() / 2);
            draggingCard.relocate(centre.getX() - draggingCard.getWidth() / 2,
                    centre.getY() - draggingCard.getHeight() / 2);
            draggingCard.relocate(
                    centre.getX() - draggingCard.getWidth() / 2,
                    centre.getY() - draggingCard.getHeight() / 2 - hand.getHeight() * 0.25);
            isAiming = true;
        }
    }

    private void showArrowToMouse(MouseEvent e) {
        if (!rootPane.getChildren().contains(arrow)) {
            rootPane.getChildren().addAll(arrow, arrowHead);
        }

        /* 把 scene 坐标统一转换成 rootPane 本地坐标 */
        Point2D start = rootPane.sceneToLocal(
                draggingCard.localToScene(
                        draggingCard.getWidth() / 2,
                        0));   // y=0 -> 顶边
        Point2D end = rootPane.sceneToLocal(e.getSceneX(), e.getSceneY());

        arrow.setStartX(start.getX()); arrow.setStartY(start.getY());
        final double GAP = ARROW_HEAD_H * 0.6;          // 0.6×箭头高 在三角里消失
        double len = Math.hypot(end.getX() - start.getX(), end.getY() - start.getY());
        double ux  = (end.getX() - start.getX()) / len; // 单位向量
        double uy  = (end.getY() - start.getY()) / len;
        arrow.setEndX(end.getX() - ux * GAP);
        arrow.setEndY(end.getY() - uy * GAP);

        /* 箭头朝向与位置 */
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double angle = Math.atan2(dy, dx);

        arrowHead.getTransforms().setAll(
                new Rotate(Math.toDegrees(angle) - 90, 0, 0));
        arrowHead.setTranslateX(end.getX());
        arrowHead.setTranslateY(end.getY());
        arrowHead.setViewOrder(-1002);
    }

    private void hideArrow() {
        if (rootPane.getChildren().contains(arrow)) {
            rootPane.getChildren().removeAll(arrow, arrowHead);
            isAiming = false;
        }
    }
    private void finishCardDrag(boolean returnToHand, Runnable afterPlay) {
        if (dragPhase != DragPhase.DRAGGING || draggingCard == null) return;
        dragPhase = DragPhase.FLYING;                // 立即锁状态
        draggingCard.setHoverEffectEnabled(true);

        Scene sc = rootPane.getScene();
        sc.removeEventFilter(MouseEvent.MOUSE_MOVED,   this::updateCardDrag);
        sc.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::globalMousePress);

        hideArrow();
        //draggingCard.setScaleX(1); draggingCard.setScaleY(1);

        if (returnToHand) {                       // 取消拖拽
            /* 动画：移动 + 缩放回 1.0 */
            CardView cardNode = draggingCard;

            cardAnimator.flyBackToHand(rootPane, cardNode, returnPos, () -> {
                rootPane.getChildren().remove(cardNode);
                cardNode.setTranslateX(0); cardNode.setTranslateY(0);
                cardNode.setManaged(true);
                if (cardNode.getParent() != dragOriginParent) {
                    dragOriginParent.getChildren()
                            .add(Math.min(dragOriginIndex,
                                            dragOriginParent.getChildren().size()),
                                    cardNode);
                }
                clearDragState();          // dragPhase = NONE, 引用清空
            });
        } else { // 成功使用
            // 把节点从原父容器移除，避免 layout 把它弹回手牌
            dragOriginParent.getChildren().remove(draggingCard); // 防止 layout 抖动
            if (afterPlay != null) afterPlay.run();         // 交给 tryUseDraggedCard 后续动画
        }
    }

    public void cancelCardDrag() {
        if (dragPhase != DragPhase.DRAGGING) return;
        finishCardDrag(true, null);
    }

    private void initArrow() {
        arrow.setStrokeLineCap(StrokeLineCap.ROUND);
        arrow.setStrokeWidth(ARROW_THICKNESS);
        arrow.setManaged(false);
        arrow.setMouseTransparent(true);
        arrow.setViewOrder(-1000);

        arrowHead.setManaged(false);
        arrowHead.setStroke(Color.TRANSPARENT);
        arrowHead.setFill(Color.GRAY);
        arrowHead.toFront();
        arrowHead.getPoints().setAll(0.0,0.0, -13.0,-26.0, 13.0,-26.0);
        arrowHead.setMouseTransparent(true);
        arrowHead.setViewOrder(-1001);
    }

    private void clearHandUI() {
        handCardBox.getChildren().clear();
    }
    ///////re

    private void refreshExhaustCount() {
        cardInExhaustLabel.setText(String.valueOf(deckManager.getExhaustPile().size()));
    }

    @FXML
    private void onSelectWarrior() {
        // 玩家选战士 初始化其他...
        player = new Player("铁血战士");
        deckManager.reset();
        deckManager.initDeckForClass("warrior");
        initWithPlayerAndDeck(player);
        switchToMode(getUIState(engine.getState()));
    }

    @FXML
    private void onSelectMage() {
        player = new Player("mage");
        deckManager.reset();
        deckManager.initDeckForClass("mage");
        initWithPlayerAndDeck(player);
        switchToMode(getUIState(engine.getState()));
    }

    @FXML
    private void onSelectRogue() {
        player = new Player("rogue");
        deckManager.reset();
        deckManager.initDeckForClass("rogue");
        initWithPlayerAndDeck(player);
        switchToMode(getUIState(engine.getState()));
    }

    public StackPane getRootPane() {
        return rootPane;
    }

    public Deque<Node> getOverlayStack() {
        return overlayStack;
    }

    public DeckManager getDeckManager() {
        return deckManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    private void syncWidths(double per) {
        infoBar.getChildren().forEach(n -> ((Region) n).setPrefWidth(per));
        btnWarrior.setPrefWidth(per);
    }

    public void centerScroll() {
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                MapNode playerNode = gameManager.getCurrentLevel().getLayeredGameMap().getPlayerNode();
                Button btn = nodeToButtonMap.get(playerNode);
                if (btn != null) {
                    centerScrollOn(mapScroll, btn);
                }
            });
        });
    }

    /** 把 scrollPane 的视口中心对齐到 node 屏幕中心 */
    private void centerScrollOn(ScrollPane scroll, Node node) {
        Bounds contentBounds = scroll.getContent().localToScene(scroll.getContent().getBoundsInLocal());
        Bounds nodeBounds    = node.localToScene(node.getBoundsInLocal());

        double viewportH = scroll.getViewportBounds().getHeight();
        double viewportW = scroll.getViewportBounds().getWidth();

        double targetX = nodeBounds.getMinX() + nodeBounds.getWidth()  / 2.0;
        double targetY = nodeBounds.getMinY() + nodeBounds.getHeight() / 2.0;

        double hValue = (targetX - viewportW / 2 - contentBounds.getMinX())
                / (contentBounds.getWidth()  - viewportW);
        double vValue = (targetY - viewportH / 2 - contentBounds.getMinY())
                / (contentBounds.getHeight() - viewportH);

        scroll.setHvalue(clamp(hValue));
        scroll.setVvalue(clamp(vValue));
    }

    private double clamp(double v) { return Math.max(0, Math.min(1, v)); }

    public Label getCardInDeckLabel() {
        return cardInDeckLabel;
    }
    public CardAnimator getCardAnimator() {
        return cardAnimator;
    }
}
