package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

public class MainMenuState extends BaseAppState {
    private Application app;
    private Node guiNode;
    private Picture logInPicture;
    private Picture registerPicture;
    private Picture visitorPicture;
    private InputManager inputManager;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application is not an instance of SimpleApplication");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();
        inputManager = app.getInputManager();

        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("");
    }

    private void initGui() {
        logInPicture = new Picture("LogIn");
        logInPicture.setImage(app.getAssetManager(), "icon.png", true);
        logInPicture.setWidth(200);
        logInPicture.setHeight(200);
        logInPicture.setLocalTranslation(10, app.getCamera().getHeight() - 200, 0);
        guiNode.attachChild(logInPicture);

        registerPicture = new Picture("Register");
        registerPicture.setImage(app.getAssetManager(), "icon.png", true);
        registerPicture.setWidth(200);
        registerPicture.setHeight(200);
        registerPicture.setLocalTranslation(10, app.getCamera().getHeight() - 400, 0);
        guiNode.attachChild(registerPicture);

        visitorPicture = new Picture("Visitor");
        visitorPicture.setImage(app.getAssetManager(), "icon.png", true);
        visitorPicture.setWidth(200);
        visitorPicture.setHeight(200);
        visitorPicture.setLocalTranslation(10, app.getCamera().getHeight() - 600, 0);
        guiNode.attachChild(visitorPicture);
    }

    private void initInput() {
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Click");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && !isPressed) {
                // 获取鼠标点击位置
                float x = inputManager.getCursorPosition().x;
                float y = inputManager.getCursorPosition().y;

                // 检查点击位置是否在图片范围内
                if (Main.inPicture(logInPicture, x, y)) {
                    getStateManager().detach(MainMenuState.this); // 移除当前状态
                    getStateManager().attach(new LoginState()); // 切换到登
                } else if (Main.inPicture(registerPicture, x, y)) {
                    getStateManager().detach(MainMenuState.this);
                    getStateManager().attach(new RegisterState());
                } else if (Main.inPicture(visitorPicture, x, y)) {
                    Main.username = "Visitor";
                    getStateManager().detach(MainMenuState.this);
                    getStateManager().attach(new LevelSelectionState());
                }
            }
        }
    };

    @Override
    public void onEnable() {
        initGui();
        initInput();
    }

    @Override
    public void onDisable() {
        logInPicture.removeFromParent();
        registerPicture.removeFromParent();
        visitorPicture.removeFromParent();

        inputManager.removeListener(actionListener);
        inputManager.clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}