package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

public class HelpState extends BaseAppState {
    private static final int TOTAL_LABELS = 5;

    private Application app;
    private Node guiNode;
    private GameState gameState;
    private Label labels[] = new Label[TOTAL_LABELS];
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
        labels[0] = new Label("Push all the boxes onto the glowing yellow target spots to win!");
        labels[1] = new Label("Movement: Use WASD to move, QE to rotate the camera, and Space to push boxes. " +
                "You can also click the corresponding buttons on the interface.");
        labels[2] = new Label("Restart: Press R to restart the current level.");
        labels[3] = new Label("Undo: Press U to undo the last move.");
        labels[4] = new Label("For more actions, press ESC or click the top-left corner to open the menu.");

        for (Label label : labels) {
            label.setFontSize(40);
            label.setLocalTranslation(100, app.getCamera().getHeight() - 100, 0);
        }
    }

    @Override
    protected void onEnable() {
        initHelp();
        guiNode.attachChild(labels[currentLabel]);

        // 激活输入监听
        InputManager inputManager = app.getInputManager();
        if (inputInterceptor == null) {
            inputInterceptor = new RawInputListener() {
                @Override
                public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {
                    if (evt.isPressed()) {
                        labels[currentLabel].removeFromParent();
                        if (currentLabel < TOTAL_LABELS - 1) {
                            currentLabel++;
                            guiNode.attachChild(labels[currentLabel]);
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
        if (currentLabel < TOTAL_LABELS) labels[currentLabel].removeFromParent();
        app.getInputManager().removeRawInputListener(inputInterceptor); // 移除输入监听
        gameState.closeHelp();
    }

    @Override
    protected void cleanup(Application app) {}
}