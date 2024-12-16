package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

import game.GameState;

public class LevelSelectionState extends BaseAppState {
    private static final int LEVEL_COUNT = 18;

    private Application app;
    private Node guiNode;
    private Container levelSelectionForm;
    private Picture welcome,select,settings;

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

    private void initGui() {
        levelSelectionForm = new Container();
        guiNode.attachChild(levelSelectionForm);

        levelSelectionForm.addChild(new Label("                    " + Main.username + " ")).setFontSize(45);
        welcome = new Picture("welcome");
        welcome.setImage(app.getAssetManager(), "welcome.png", true);
        welcome.setWidth(200);
        welcome.setHeight(36);
        welcome.setLocalTranslation(15, app.getCamera().getHeight()-58 , 0);
        guiNode.attachChild(welcome);

        /*Button settingButton = levelSelectionForm.addChild(new Button("Settings"));
        settingButton.setFontSize(45);
        settingButton.addClickCommands(source -> {
            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new SettingState()); // 切换到 SettingState
        });*/
        levelSelectionForm.addChild(new Label("                   ")).setFontSize(45);
        settings = new Picture("settings");
        settings.setImage(app.getAssetManager(), "buttonsettings.png", true);
        settings.setWidth(350);
        settings.setHeight(70);
        settings.setLocalTranslation(12, app.getCamera().getHeight()-130 , 0);
        guiNode.attachChild(settings);

        levelSelectionForm.addChild(new Label("                   ")).setFontSize(45);
        select = new Picture("select");
        select.setImage(app.getAssetManager(), "select.png", true);
        select.setWidth(350);
        select.setHeight(44);
        select.setLocalTranslation(12, app.getCamera().getHeight()-185 , 0);
        guiNode.attachChild(select);

        for (int i = 1; i <= LEVEL_COUNT; i++) {
            Button levelButton = levelSelectionForm.addChild(new Button("Level " + i));
            levelButton.setFontSize(24);
            final int level = i;
            levelButton.addClickCommands(source -> {
                Main.removeBackground(app); // 移除背景图片

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
        initGui(); // 初始化 GUI
        guiNode.attachChild(levelSelectionForm); // 将菜单添加到 GUI 节点
    }

    @Override
    public void onDisable() {
        for (Spatial child : levelSelectionForm.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        levelSelectionForm.detachAllChildren(); // 移除所有子节点
        levelSelectionForm.removeFromParent(); // 将菜单从 GUI 节点移除
        app.getInputManager().clearMappings();
        welcome.removeFromParent();
        select.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {}
}