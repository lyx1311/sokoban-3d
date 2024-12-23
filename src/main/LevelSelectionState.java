package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
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
    public static final int LEVEL_COUNT = 18;

    private Application app;
    private Node guiNode;
    private Label usernameLabel;
    private Container levelSelectionForm;
    private Picture welcome,select,settings,back;

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
        app.getInputManager().addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "Click");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && isPressed) {
                // 获取鼠标点击位置
                float x = app.getInputManager().getCursorPosition().x;
                float y = app.getInputManager().getCursorPosition().y;

                // 检查点击位置是否在图片范围内
                if (Main.inPicture(settings, x, y)) {
                    Main.playClickSound();
                    getStateManager().detach(LevelSelectionState.this); // 移除当前状态
                    getStateManager().attach(new SettingState()); // 切换到登
                } else if (Main.inPicture(back, x, y)) {
                    Main.playClickSound();
                    getStateManager().detach(LevelSelectionState.this);
                    getStateManager().attach(new MainMenuState());
                }
            }
        }
    };

    private void initGui() {
        welcome = new Picture("welcome");
        welcome.setImage(app.getAssetManager(), "welcome.png", true);
        welcome.setWidth(200);
        welcome.setHeight(36);
        welcome.setLocalTranslation(15, app.getCamera().getHeight() - 58 , 0);
        guiNode.attachChild(welcome);

        usernameLabel = new Label(Main.username);
        usernameLabel.setFontSize(45);
        usernameLabel.setLocalTranslation(230, app.getCamera().getHeight() - 10, 0);
        usernameLabel.setColor(new com.jme3.math.ColorRGBA(1, 1, 1, 1));
        guiNode.attachChild(usernameLabel);

        /*settingButton = new Button("Settings");
        settingButton.setFontSize(45);
        settingButton.setLocalTranslation(10, app.getCamera().getHeight() - 60, 0);
        settingButton.addClickCommands(source -> {
            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new SettingState()); // 切换到 SettingState
        });
        guiNode.attachChild(settingButton);*/

        settings = new Picture("settings");
        settings.setImage(app.getAssetManager(), "buttonsettings.png", true);
        settings.setWidth(275);
        settings.setHeight(70);
        settings.setLocalTranslation(55, app.getCamera().getHeight()-675 , 0);
        guiNode.attachChild(settings);

        select = new Picture("select");
        select.setImage(app.getAssetManager(), "select.png", true);
        select.setWidth(350);
        select.setHeight(44);
        select.setLocalTranslation(12, app.getCamera().getHeight() - 185 , 0);
        guiNode.attachChild(select);

        levelSelectionForm = new Container();
        guiNode.attachChild(levelSelectionForm);
        for (int i = 1; i <= LEVEL_COUNT; i++) {
            Button levelButton = levelSelectionForm.addChild(new Button("Level " + i + "        "),
                    (i - 1) / 3, (i - 1) % 3);
            levelButton.setFontSize(40);
            levelButton.setColor(new com.jme3.math.ColorRGBA(1, 1, 1, 1));
            final int level = i;
            levelButton.addClickCommands(source -> {
                Main.playClickSound();
                Main.removeBackground(app); // 移除背景图片

                getStateManager().detach(this); // 移除当前状态
                getStateManager().attach(new GameState(level)); // 切换到 GameState
            });
        }
        levelSelectionForm.setLocalTranslation(10, app.getCamera().getHeight() - 200, 0); // 设置窗口位置


        /*backButton = new Button("Log Out and Back");
        backButton.setFontSize(24);
        backButton.setLocalTranslation(10, app.getCamera().getHeight() - 600, 0);
        backButton.addClickCommands(source -> {
            Main.username = ""; // 重置用户名

            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new MainMenuState()); // 切换到 MainMenuState
        });
        guiNode.attachChild(backButton);*/

        back = new Picture("back");
        back.setImage(app.getAssetManager(), "back.png", true);
        back.setWidth(467);
        back.setHeight(70);
        back.setLocalTranslation(55, app.getCamera().getHeight() - 800 , 0);
        guiNode.attachChild(back);
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        guiNode.attachChild(levelSelectionForm); // 将菜单添加到 GUI 节点
        initInput();
    }

    @Override
    public void onDisable() {
        for (Spatial child : levelSelectionForm.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }

        usernameLabel.removeFromParent();
        settings.removeFromParent();
        back.removeFromParent();
        levelSelectionForm.detachAllChildren(); // 移除所有子节点
        levelSelectionForm.removeFromParent(); // 将菜单从 GUI 节点移除
        app.getInputManager().clearMappings();
        welcome.removeFromParent();
        select.removeFromParent();
        app.getInputManager().removeListener(actionListener);
        app.getInputManager().clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}