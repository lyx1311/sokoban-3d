package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
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
    private Container menu;
    private Label stepsLabel;

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
        initGui();
    }

    private void initGui() {
        menu = new Container();

        Label levelLabel = menu.addChild(new Label("Level " + level));
        levelLabel.setFontSize(30);

        stepsLabel = menu.addChild(new Label("Steps: " + cubeState.getSteps()));
        stepsLabel.setFontSize(24);

        Button solveButton = menu.addChild(new Button(gameState.isSolving() ? "Stop AI" : "Solve by AI"));
        solveButton.setFontSize(24);
        solveButton.addClickCommands(source -> {
            gameState.closeMenu();

            if (gameState.isSolving()) {
                gameState.stopSolving();
            } else {
                if (checkWorking()) return;

                gameState.startSolving();
                getStateManager().attach(new AlertState(
                        "AI Solving",
                        "Please wait for a moment."
                ));
            }
        });

        Button restartButton = menu.addChild(new Button("Restart"));
        restartButton.setFontSize(24);
        restartButton.addClickCommands(source -> {
            if(checkWorking()) return;

            cubeState.restart();

            gameState.closeMenu();
        });

        if (!Main.username.equals("Visitor")) {
            Button loadButton = menu.addChild(new Button("Load"));
            loadButton.setFontSize(24);
            loadButton.addClickCommands(source -> {
                if(checkWorking()) return;

                cubeState.load();
                gameState.closeMenu();
            });

            Button saveButton = menu.addChild(new Button("Save"));
            saveButton.setFontSize(24);
            saveButton.addClickCommands(source -> {
                if(checkWorking()) return;

                cubeState.save();
                gameState.closeMenu();
            });
        }

        Button backButton = menu.addChild(new Button("Back (no save)"));
        backButton.setFontSize(24);
        if (Main.username.equals("Visitor")) backButton.setText("Back");
        backButton.addClickCommands(source -> {
            if(checkWorking()) return;

            // if (!Main.username.equals("Visitor")) cubeState.save();

            gameState.closeMenu(); // 关闭菜单
            getStateManager().detach(gameState); // 移除游戏状态
            getStateManager().attach(new LevelSelectionState()); // 切换到游戏状态
        });

        Button closeButton = menu.addChild(new Button("Close Menu"));
        closeButton.setFontSize(24);
        closeButton.addClickCommands(source -> gameState.closeMenu());

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 1);
    }

    public void updateSteps() { stepsLabel.setText("Steps: " + cubeState.getSteps()); }

    private boolean checkWorking() {
        if (gameState.isWorking()) {
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
    protected void onEnable() { guiNode.attachChild(menu); }

    @Override
    protected void onDisable() {
        for (Spatial child : menu.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        menu.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {}
}