package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

import game.GameState;

public class LevelSelectionState extends BaseAppState {
    private static final int LEVEL_COUNT = 8;

    private Application app;
    private Node guiNode;
    private Container levelSelectionForm;

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
        levelSelectionForm = new Container();
        guiNode.attachChild(levelSelectionForm);

        levelSelectionForm.addChild(new Label("Hello, " + Main.username + "!")).setFontSize(24);
        levelSelectionForm.addChild(new Label("Select a level:")).setFontSize(24);

        for (int i = 1; i <= LEVEL_COUNT; i++) {
            Button levelButton = levelSelectionForm.addChild(new Button("Level " + i));
            levelButton.setFontSize(24);
            final int level = i;
            levelButton.addClickCommands(source -> {
                // 删除背景图片
                boolean foundBackground = false;
                for (Spatial child : guiNode.getChildren()) {
                    if (child.getName() != null && child.getName().equals("Background")) {
                        foundBackground = true;
                        child.removeFromParent();
                        break;
                    }
                }
                if (!foundBackground) {
                    System.err.println("Background not found when switching to GameState.");
                }

                getStateManager().detach(this); // 移除当前状态
                getStateManager().attach(new GameState(level)); // 切换到 GameState
            });
        }

        Button backButton = levelSelectionForm.addChild(new Button("Log Out and Back"));
        backButton.setFontSize(24);
        backButton.addClickCommands(source -> {
            Main.username = ""; // 重置用户名

            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new MainMenuState()); // 切换到 MainMenuState
        });

        // 设置窗口位置
        levelSelectionForm.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
    }

    @Override
    public void onEnable() {
        guiNode.attachChild(levelSelectionForm); // 将菜单添加到 GUI 节点
    }

    @Override
    public void onDisable() {
        for (Spatial child : levelSelectionForm.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        levelSelectionForm.removeFromParent(); // 将菜单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}
