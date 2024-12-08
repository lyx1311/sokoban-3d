package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import main.LevelSelectionState;
import main.Main;

public class MenuState extends BaseAppState {
    private Application app;
    private final GameState gameState;
    private final CubeState cubeState;
    private Node guiNode;
    private Container menu;

    public MenuState(GameState gameState, CubeState cubeState) {
        this.gameState = gameState;
        this.cubeState = cubeState;
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

        Button restartButton = menu.addChild(new Button("Restart"));
        restartButton.setFontSize(24);
        restartButton.addClickCommands(source -> {
            cubeState.restart();
            gameState.assertInstructionsCleared();
            getStateManager().detach(this);
            gameState.setMenuOpen(false);
        });

        if (!Main.username.equals("Visitor")) {
            Button loadButton = menu.addChild(new Button("Load"));
            loadButton.setFontSize(24);
            loadButton.addClickCommands(source -> {
                cubeState.load();
                getStateManager().detach(this);
                gameState.setMenuOpen(false);
            });

            Button saveButton = menu.addChild(new Button("Save"));
            saveButton.setFontSize(24);
            saveButton.addClickCommands(source -> {
                cubeState.save();
                getStateManager().detach(this);
                gameState.setMenuOpen(false);
            });
        }

        Button backButton = menu.addChild(new Button("Back (no save)"));
        backButton.setFontSize(24);
        if (Main.username.equals("Visitor")) backButton.setText("Back");

        // 设置按钮点击事件
        backButton.addClickCommands(source -> {
            // if (!Main.username.equals("Visitor")) cubeState.save();

            getStateManager().detach(this); // 移除当前状态
            gameState.setMenuOpen(false); // 关闭菜单
            getStateManager().detach(gameState); // 移除游戏状态
            getStateManager().attach(new LevelSelectionState()); // 切换到游戏状态
        });

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 1);
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
