package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
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
        //将按键替换成图片
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }
    private Spatial getPicture() {

        // 获得屏幕分辨率
        float width = app.getCamera() .getWidth();
        float height = app.getCamera().getHeight();

        // 创建一个四边形
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry("Picture", quad);

        // 加载图片
        Texture tex = app.getAssetManager().loadTexture("Textures/interface.jpg");

        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);

        // 应用这个材质
        geom.setMaterial(mat);

        return geom;
    }
    private void initGui() {
        menu = new Container();
        Button pic = menu.addChild(new Button("pic"));
        guiNode.attachChild(pic);
        guiNode.attachChild(getPicture());


        stepsLabel = menu.addChild(new Label("Steps: " + cubeState.getSteps()));
        stepsLabel.setFontSize(24);

        if (!cubeState.isWin()) {
            Button solveButton = menu.addChild(new Button(gameState.isSolving() ? "Stop AI" : "Solve by AI"));
            solveButton.setFontSize(24);
            solveButton.addClickCommands(source -> {
                if (checkSolverWorking()) return;

                if (cubeState.isWin()) {
                    getStateManager().attach(new AlertState(
                            "You've Won",
                            "Can't solve a solved level."
                    ));
                    return;
                }

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
        }

        Button restartButton = menu.addChild(new Button("Restart"));
        restartButton.setFontSize(24);
        restartButton.addClickCommands(source -> {
            if (checkSolverWorking()) return;
            if (checkWorking()) return;

            cubeState.restart();
            gameState.closeMenu();
        });

        if (!Main.username.equals("Visitor")) {
            Button loadButton = menu.addChild(new Button("Load"));
            loadButton.setFontSize(24);
            loadButton.addClickCommands(source -> {
                if (checkSolverWorking()) return;
                if (checkWorking()) return;

                if (cubeState.load()) gameState.closeMenu();
            });

            Button saveButton = menu.addChild(new Button("Save"));
            saveButton.setFontSize(24);
            saveButton.addClickCommands(source -> {
                if (checkSolverWorking()) return;
                if (checkWorking()) return;

                cubeState.save();
                gameState.closeMenu();
            });
        }

        Button backButton = menu.addChild(new Button("Back (no save)"));
        backButton.setFontSize(24);
        if (Main.username.equals("Visitor")) backButton.setText("Back");
        backButton.addClickCommands(source -> {
            if (checkSolverWorking()) return;
            if (checkWorking()) return;

            // if (!Main.username.equals("Visitor")) cubeState.save();

            gameState.closeMenu(); // 关闭菜单
            getStateManager().detach(gameState); // 移除游戏状态
            getStateManager().attach(new LevelSelectionState()); // 切换到游戏状态

            Main.createBackground(app); // 创建背景图片
        });

        Button closeButton = menu.addChild(new Button("Close Menu"));
        closeButton.setFontSize(24);
        closeButton.addClickCommands(source -> gameState.closeMenu());

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 1);
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
        guiNode.attachChild(menu); // 将菜单添加到 GUI 节点
    }

    @Override
    protected void onDisable() {
        for (Spatial child : menu.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        menu.detachAllChildren(); // 移除所有子节点
        menu.removeFromParent(); // 将菜单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}