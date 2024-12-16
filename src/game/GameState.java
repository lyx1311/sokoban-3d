package game;

import java.util.LinkedList;
import java.util.Queue;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import main.Main;

public class GameState extends BaseAppState {
    private Application app;
    private int level;
    private InputManager inputManager;
    private CubeState cubeState;
    private MenuState menuState;
    private boolean isMenuOpen = false, isHelping = false, isSolving = false, isSolverWorking = false;
    private Queue<String> instructions = new LinkedList<>();
    private Queue<Character> solution = new LinkedList<>();
    private Picture menu;
    private Node guiNode;

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
    }


    private void initGui(){
        menu = new Picture("menu");
        menu.setImage(app.getAssetManager(), "menu.png", true);
        menu.setWidth(100);
        menu.setHeight(100);
        menu.setLocalTranslation(15, app.getCamera().getHeight() - 100 , 0);
        guiNode.attachChild(menu);
    }

    public boolean isSolving() { return isSolving; }
    public boolean isSolverWorking() { return isSolverWorking; }
    public boolean isWorking() { return cubeState.inMotion() || !instructions.isEmpty(); }

    public void openMenu() {
        menuState = new MenuState(this, cubeState, level);
        getStateManager().attach(menuState);
        menu.removeFromParent();
        isMenuOpen = true;
    }
    public void closeMenu() {
        getStateManager().detach(menuState);
        initGui();
        isMenuOpen = false;
    }

    public void openHelp() {
        HelpState helpState = new HelpState(this);
        getStateManager().attach(helpState);
        isHelping = true;
    }
    public void closeHelp() { isHelping = false; }

    public void startSolving() {
        isSolverWorking = true;
        if (cubeState.isFlying()) cubeState.reverseFly();

        cubeState.solve(steps -> {
            isSolverWorking = false;
            isSolving = true;

            if (steps == null) {
                System.out.println("No solution found");
                stopSolving();
            } else {
                System.out.println("Solution: " + steps);
                for (char step : steps.toCharArray()) solution.add(step);
            }
        });
    }
    public void stopSolving() {
        isSolving = false;
        solution.clear();

        System.out.println("Solving stopped");
    }

    private void initInput() {
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
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight",
                "PushBox", "RotateLeft", "RotateRight", "Undo", "Fly", "OpenMenu","Click");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isHelping) return;

            if (isPressed) {
                if (name.equals("Click")) {
                    float x = app.getInputManager().getCursorPosition().x;
                    float y = app.getInputManager().getCursorPosition().y;
                    if (Main.inPicture(menu, x, y) && !isMenuOpen) openMenu();
                }
                if (name.equals("OpenMenu")) {
                    if (isMenuOpen) {
                        closeMenu();
                    } else {
                        openMenu();
                    }
                    return;
                }
                if (isSolving || isSolverWorking) return;
                switch (name) {
                    case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight": case "PushBox":
                        System.out.println(" > " + name);
                        if (cubeState.isFlying()) {
                            cubeState.startMoveFlyCam(name);
                        } else {
                            instructions.add(name);
                        }
                        break;
                    case "RotateLeft": case "RotateRight":
                        if (cubeState.isFlying()) {
                            if (!cubeState.inMotion()) cubeState.rotateCamera(name.equals("RotateLeft") ? 90 : -90);
                        } else {
                            instructions.add(name);
                        }
                        break;
                    case "Undo":
                        if (!cubeState.isFlying()) instructions.add(name);
                        break;
                    case "Fly":
                        if (!isWorking()) cubeState.reverseFly();
                        break;
                }
                System.out.println("Camera position: " + app.getCamera().getLocation());
                System.out.println("Camera rotation: " + app.getCamera().getRotation());
            } else {
                switch (name) {
                    case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight": case "PushBox":
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
        initInput();
        initGui();
    }

    @Override
    public void update(float tpf) {
        if (!instructions.isEmpty() && !cubeState.inMotion()) {
            String instruction = instructions.poll();
            switch (instruction) {
                case "MoveForward": case "MoveBackward": case "MoveLeft": case "MoveRight":
                    cubeState.moveHero(instruction);
                    if (isMenuOpen) menuState.updateSteps();
                    break;
                case "PushBox":
                    cubeState.pushBox();
                    if (isMenuOpen) menuState.updateSteps();
                    break;
                case "RotateLeft": cubeState.rotateCamera(90); break;
                case "RotateRight": cubeState.rotateCamera(-90); break;
                case "Undo":
                    cubeState.undo();
                    if (isMenuOpen) menuState.updateSteps();
                    break;
            }
        }

        if (isSolving && !isWorking()) {
            if (solution.isEmpty()) {
                stopSolving();
            } else {
                char c = solution.peek();
                if (Character.isUpperCase(c)) {
                    if(cubeState.rotateTo(c)) return;
                    cubeState.pushBox(c);
                } else {
                    cubeState.moveHero(c);
                }
                if (isMenuOpen) menuState.updateSteps();
                solution.poll();
            }
        }

        if (cubeState.isWin() && !cubeState.inMotion() && !cubeState.isFlying()) {
            cubeState.reverseFly();
            if (isMenuOpen) closeMenu();
            openMenu();
        }
    }

    @Override
    public void onDisable() {
        getStateManager().detach(cubeState);
        inputManager.removeListener(actionListener);
        menu.removeFromParent();
        inputManager.clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}