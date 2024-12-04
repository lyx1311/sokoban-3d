package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

import main.LevelSelectionState;
import main.Main;

public class GameState extends BaseAppState {
    private Application app;
    private int level;
    private Node guiNode;
    private Container menu;
    private InputManager inputManager;
    private CubeState cubeState;

    public GameState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        this.inputManager = app.getInputManager();

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

        initInput();
    }

    private void initGui() {
        menu = new Container();

        Button undoButton = menu.addChild(new Button("Undo (U)"));
        undoButton.setFontSize(24);
        undoButton.addClickCommands(source -> cubeState.undo());

        Button restartButton = menu.addChild(new Button("Restart"));
        restartButton.setFontSize(24);
        restartButton.addClickCommands(source -> cubeState.restart());

        if (!Main.username.equals("Visitor")) {
            Button saveButton = menu.addChild(new Button("Save"));
            saveButton.setFontSize(24);
            saveButton.addClickCommands(source -> cubeState.save());
        }

        Button backButton = menu.addChild(new Button("Back (no save)"));
        backButton.setFontSize(24);
        if (Main.username.equals("Visitor")) backButton.setText("Back");

        // 设置按钮点击事件
        backButton.addClickCommands(source -> {
            // if (!Main.username.equals("Visitor")) cubeState.save();

            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new LevelSelectionState()); // 切换到游戏状态
        });

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 1);
    }

    private void initInput() {
        inputManager.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("PushBox", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("RotateLeft", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("RotateRight", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Undo", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Fly", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addListener(actionListener, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight",
                "PushBox", "RotateLeft", "RotateRight", "Undo", "Fly");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                switch (name) {
                    case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight":
                        System.out.println(" > " + name);
                        if (cubeState.isFlying()) cubeState.startMoveFlyCam(name);
                        else cubeState.moveHero(name);
                        break;
                    case "PushBox": cubeState.pushBox(); break;
                    case "RotateLeft": cubeState.rotateCamera(90); break;
                    case "RotateRight": cubeState.rotateCamera(-90); break;
                    case "Undo": cubeState.undo(); break;
                    case "Fly": cubeState.reverseFly(); break;
                }
                System.out.println("Camera position: " + app.getCamera().getLocation());
                System.out.println("Camera rotation: " + app.getCamera().getRotation());
            } else {
                switch (name) {
                    case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight":
                        if (cubeState.isFlying()) cubeState.stopMoveFlyCam();
                        break;
                }
            }
        }
    };

    @Override
    public void onEnable() {
        cubeState = new CubeState(level);
        getStateManager().attach(cubeState);

        guiNode.attachChild(menu);
    }

    @Override
    public void onDisable() {
        getStateManager().detach(cubeState);

        for (Spatial child : menu.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        menu.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {}
}