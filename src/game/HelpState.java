package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import main.Main;

public class HelpState extends BaseAppState {
    private static final int TOTAL_LABELS = 5;

    private Application app;
    private Node guiNode;
    private GameState gameState;
    private Picture pictures[] = new Picture[TOTAL_LABELS];
    private RawInputListener inputInterceptor; // 用于监测鼠标输入
    private int currentLabel = 0;

    public HelpState(GameState gameState) { this.gameState = gameState; }

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();

        // 初始化 GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    private void initHelp() {
        for (int i = 0; i < TOTAL_LABELS; i++) {
            pictures[i] = new Picture("help" + (i + 1));
            pictures[i].setImage(app.getAssetManager(), "help" + (i + 1) + ".png", true);
            pictures[i].setWidth(1199);
            pictures[i].setHeight(1079);
            pictures[i].setLocalTranslation(220, app.getCamera().getHeight() - 950, 0);
        }
    }

    @Override
    protected void onEnable() {
        initHelp();
        guiNode.attachChild(pictures[currentLabel]);

        // 激活输入监听
        InputManager inputManager = app.getInputManager();
        if (inputInterceptor == null) {
            inputInterceptor = new RawInputListener() {
                @Override
                public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {
                    if (evt.isPressed()) {
                        Main.playClickSound();
                        pictures[currentLabel].removeFromParent();
                        if (currentLabel < TOTAL_LABELS - 1) {
                            currentLabel++;
                            guiNode.attachChild(pictures[currentLabel]);
                        } else {
                            getStateManager().detach(HelpState.this);
                        }
                    }
                }

                @Override
                public void beginInput() {}

                @Override
                public void endInput() {}

                @Override
                public void onMouseMotionEvent(com.jme3.input.event.MouseMotionEvent evt) {}

                @Override
                public void onKeyEvent(com.jme3.input.event.KeyInputEvent evt) {}

                @Override
                public void onTouchEvent(com.jme3.input.event.TouchEvent evt) {}

                @Override
                public void onJoyAxisEvent(com.jme3.input.event.JoyAxisEvent evt) {}

                @Override
                public void onJoyButtonEvent(com.jme3.input.event.JoyButtonEvent evt) {}
            };
        }
        inputManager.addRawInputListener(inputInterceptor);
    }

    @Override
    protected void onDisable() {
        if (currentLabel < TOTAL_LABELS) pictures[currentLabel].removeFromParent();
        app.getInputManager().removeRawInputListener(inputInterceptor); // 移除输入监听
        gameState.closeHelp();
    }

    @Override
    protected void cleanup(Application app) {}
}