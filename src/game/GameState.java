package game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GameState extends BaseAppState {
    private Application app;
    private InputManager inputManager;
    private int level;
    private CubeState cubeState;

    public GameState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        this.inputManager = app.getInputManager();

        initInput();
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
                if (cubeState.inMotion()) return;
                switch (name) {
                    case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight":
                        cubeState.move(name);
                        break;
                    case "PushBox":
                        cubeState.pushBox();
                        break;
                    case "RotateLeft":
                        cubeState.rotateCamera(90);
                        break;
                    case "RotateRight":
                        cubeState.rotateCamera(-90);
                        break;
                    case "Undo":
                        cubeState.undo();
                        break;
                    case "Fly":
                        cubeState.startFlying();
                        break;
                }
                System.out.println("Camera position: " + app.getCamera().getLocation());
                System.out.println("Camera rotation: " + app.getCamera().getRotation());
            } else {
                if (name.equals("Fly")) cubeState.stopFlying();
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
    }

    @Override
    protected void cleanup(Application app) {}
}
