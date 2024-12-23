package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioNode;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

import main.AlertState;
import main.LevelSelectionState;
import main.Main;

public class MenuState extends BaseAppState {
    private Application app;
    private final GameState gameState;
    private final CubeState cubeState;
    private int level;
    private Node guiNode;
    private Container menu, wallet;
    private Label stepsLabel, timeLabel;
    private Picture help, solve, restart, back, close, load, save;
    private AudioNode backgroundMusic;

    public MenuState(GameState gameState, CubeState cubeState, int level) {
        this.gameState = gameState;
        this.cubeState = cubeState;
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (app instanceof SimpleApplication) {
            guiNode = ((SimpleApplication) app).getGuiNode();
        } else {
            throw new IllegalArgumentException("Application is not an instance of SimpleApplication");
        }

        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    private void initInput() {
        app.getInputManager().addMapping("ClickMenu", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "ClickMenu");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("ClickMenu") && isPressed) {
                // 获取鼠标点击位置
                float x = app.getInputManager().getCursorPosition().x;
                float y = app.getInputManager().getCursorPosition().y;

                if (Main.inPicture(help, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;
                    if (checkWorking()) return;

                    gameState.closeMenu();
                    gameState.openHelp();
                } else if (!Main.username.equals("Visitor") && Main.inPicture(solve, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;

                    if (cubeState.isWin()) {
                        getStateManager().attach(new AlertState(
                                "You've Won",
                                "Can't solve a solved level."
                        ));
                        return;
                    }

                    if (Main.getLevelStatus(level) == Main.UNSOLVED && !Main.buySolver(level)) {
                        getStateManager().attach(new AlertState(
                                "Not Enough Money :(",
                                "Please solve the level to get $" + Main.SOLVE_GAIN + "!"
                        ));
                        return;
                    }

                    if (gameState.isSolving()) {
                        gameState.stopSolving();
                    } else {
                        if (checkWorking()) return;

                        gameState.startSolving();
                    }

                    gameState.closeMenu();
                } else if (Main.inPicture(restart, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;
                    if (checkWorking()) return;

                    cubeState.restart();
                    gameState.closeMenu();
                } else if (!Main.username.equals("Visitor") && Main.inPicture(load, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;
                    if (checkWorking()) return;
                    if (cubeState.load()) gameState.closeMenu();
                } else if (!Main.username.equals("Visitor") && Main.inPicture(save, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;
                    if (checkWorking()) return;
                    cubeState.save();
                    gameState.closeMenu();
                } else if (Main.inPicture(back, x, y)) {
                    Main.playClickSound();
                    if (checkSolverWorking()) return;
                    if (checkWorking()) return;

                    // if (!Main.username.equals("Visitor")) cubeState.save();

                    gameState.closeMenu(); // 关闭菜单
                    getStateManager().detach(gameState); // 移除游戏状态
                    getStateManager().attach(new LevelSelectionState()); // 切换到游戏状态

                    Main.createBackground(app); // 创建背景图片
                } else if (Main.inPicture(close, x, y)) {
                    Main.playClickSound();
                    gameState.closeMenu();
                }
            }
        }
    };

    private void initGui() {
        initMenu();
        guiNode.attachChild(menu); // 将菜单添加到 GUI 节点
        if (!Main.username.equals("Visitor")) {
            initWallet();
            guiNode.attachChild(wallet); // 将钱包添加到 GUI 节点
        }
    }

    private void initMenu() {
        menu = new Container();

        Label levelLabel = menu.addChild(new Label("Level " + level));
        levelLabel.setColor(new ColorRGBA(1, 1, 1, 1));
        levelLabel.setFontSize(36);

        stepsLabel = menu.addChild(new Label("Steps: " + cubeState.getSteps()));
        stepsLabel.setColor(new ColorRGBA(1, 1, 1, 1));
        stepsLabel.setFontSize(24);

        timeLabel = menu.addChild(new Label("Rest Time: " + cubeState.getTimeLeft() + "s"));
        timeLabel.setColor(new ColorRGBA(1, 1, 1, 1));
        timeLabel.setFontSize(24);

        help = new Picture("help");
        help.setImage(app.getAssetManager(), "menuhelp.png", true);
        help.setWidth(122);
        help.setHeight(50);
        help.setLocalTranslation(10, app.getCamera().getHeight() - 190, 0);
        guiNode.attachChild(help);

        restart = new Picture("restart");
        restart.setImage(app.getAssetManager(), "menurestart.png", true);
        restart.setWidth(188);
        restart.setHeight(50);
        restart.setLocalTranslation(10, app.getCamera().getHeight() - 240, 0);
        guiNode.attachChild(restart);

        if (!Main.username.equals("Visitor")) {
            load = new Picture("load");
            load.setImage(app.getAssetManager(), "menuload.png", true);
            load.setWidth(131);
            load.setHeight(50);
            load.setLocalTranslation(10, app.getCamera().getHeight() - 290, 0);
            guiNode.attachChild(load);

            save = new Picture("save");
            save.setImage(app.getAssetManager(), "menusave.png", true);
            save.setWidth(137);
            save.setHeight(50);
            save.setLocalTranslation(10, app.getCamera().getHeight() - 340, 0);
            guiNode.attachChild(save);

            if (gameState.isSolving()) {
                solve = new Picture("stop");
                solve.setImage(app.getAssetManager(), "menustop.png", true);
                solve.setWidth(183);
            } else {
                solve = new Picture("solve");
                solve.setImage(app.getAssetManager(), "menusolve.png", true);
                solve.setWidth(257);
            }
            solve.setHeight(50);
            solve.setLocalTranslation(10, app.getCamera().getHeight() - 390, 0);
            guiNode.attachChild(solve);

            back = new Picture("back");
            back.setImage(app.getAssetManager(), "menuback.png", true);
            back.setWidth(135);
            back.setHeight(50);
            back.setLocalTranslation(10, app.getCamera().getHeight() - 440, 0);
            guiNode.attachChild(back);

            close = new Picture("close");
            close.setImage(app.getAssetManager(), "menuclose.png", true);
            close.setWidth(178);
            close.setHeight(50);
            close.setLocalTranslation(10, app.getCamera().getHeight() - 490, 0);
            guiNode.attachChild(close);
        } else {
            back = new Picture("back");
            back.setImage(app.getAssetManager(), "menuback.png", true);
            back.setWidth(135);
            back.setHeight(50);
            back.setLocalTranslation(10, app.getCamera().getHeight() - 290, 0);
            guiNode.attachChild(back);

            close = new Picture("close");
            close.setImage(app.getAssetManager(), "menuclose.png", true);
            close.setWidth(178);
            close.setHeight(50);
            close.setLocalTranslation(10, app.getCamera().getHeight() - 340, 0);
            guiNode.attachChild(close);
        }

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 1);
    }

    private void initWallet() {
        wallet = new Container();

        Label walletMoney = wallet.addChild(new Label("Money: $" + Main.getMoney()));
        walletMoney.setColor(new ColorRGBA(1, 1, 1, 1));
        walletMoney.setFontSize(32);

        Label walletHint1 = wallet.addChild(new Label(Main.getLevelStatus(level) == Main.UNSOLVED ?
                "Solve the level in time to get $" + Main.SOLVE_GAIN + "!" :
                "You've solved the level!"
        ));
        walletHint1.setColor(new ColorRGBA(1, 1, 1, 1));
        walletHint1.setFontSize(20);

        Label walletHint2 = wallet.addChild(new Label(Main.getLevelStatus(level) == Main.SOLVER_BOUGHT ?
                "You've bought the solver!" : Main.getLevelStatus(level) == Main.UNSOLVED ?
                "Buy a solver for $" + Main.SOLVER_COST + "!" : "You've solved it, so solver is free!"
        ));
        walletHint2.setColor(new ColorRGBA(1, 1, 1, 1));
        walletHint2.setFontSize(20);

        // 设置窗口位置
        wallet.setLocalTranslation(app.getCamera().getWidth() - 305, app.getCamera().getHeight() - 10, 1);
    }

    public void updateSteps() { stepsLabel.setText("Steps: " + cubeState.getSteps()); }

    private boolean checkSolverWorking() {
        if (gameState.isSolverWorking()) {
            getStateManager().attach(new AlertState(
                    "AI Finding Solution",
                    "Please wait for a moment."
            ));
            return true;
        } else {
            return false;
        }
    }

    private boolean checkWorking() {
        if (gameState.isSolving() || gameState.isWorking()) {
            getStateManager().attach(new AlertState(
                    "You Are Moving",
                    "Please wait for a moment."
            ));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onEnable() {
        initGui(); // 初始化 GUI
        initInput(); // 初始化输入监听
    }

    @Override
    public void update(float tpf) { timeLabel.setText("Time Left: " + cubeState.getTimeLeft() + "s"); }

    @Override
    protected void onDisable() {
        for (Spatial child : menu.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        menu.detachAllChildren(); // 移除所有子节点
        menu.removeFromParent(); // 将菜单从 GUI 节点移除

        help.removeFromParent();
        restart.removeFromParent();
        back.removeFromParent();
        close.removeFromParent();

        if (!Main.username.equals("Visitor")) {
            wallet.detachAllChildren(); // 移除所有子节点
            wallet.removeFromParent(); // 将钱包从 GUI 节点移除
            load.removeFromParent();
            save.removeFromParent();
            solve.removeFromParent();
        }

        app.getInputManager().removeListener(actionListener);
        app.getInputManager().deleteMapping("ClickMenu");
    }

    @Override
    protected void cleanup(Application app) {}
}