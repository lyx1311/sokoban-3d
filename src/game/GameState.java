package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GameState extends BaseAppState {
    private Application app;
    private int level;
    private InputManager inputManager;
    private CubeState cubeState;
    private MenuState menuState;
    private boolean isMenuOpen = false;

    public GameState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        this.inputManager = app.getInputManager();

        initInput();
    }

    public void setMenuOpen(boolean isMenuOpen) { this.isMenuOpen = isMenuOpen; }

    private void initInput() {
        // 移除空格键的默认映射
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);

        // 添加自定义输入映射
        inputManager.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("PushBox", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("RotateLeft", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("RotateRight", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Undo", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Fly", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("OpenMenu", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight",
                "PushBox", "RotateLeft", "RotateRight", "Undo", "Fly", "OpenMenu");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                if (name.equals("OpenMenu") && !cubeState.inMotion()) {
                    if (isMenuOpen) {
                        getStateManager().detach(menuState);
                        isMenuOpen = false;
                        System.out.println("Close menu");
                    } else {
                        menuState = new MenuState(GameState.this, cubeState);
                        getStateManager().attach(menuState);
                        isMenuOpen = true;
                    }
                    return;
                }
                if (isMenuOpen) return;
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
    }

    @Override
    public void onDisable() {
        getStateManager().detach(cubeState);
        inputManager.removeListener(actionListener);
    }

    @Override
    protected void cleanup(Application app) {}
}