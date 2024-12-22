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
    private Picture menu, fd, bk, l, r, Q, E, space;
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

    private void initMenu() {
        menu = new Picture("menu");
        menu.setImage(app.getAssetManager(), "menu.png", true);
        menu.setWidth(100);
        menu.setHeight(100);
        menu.setLocalTranslation(15, app.getCamera().getHeight() - 100, 0);
        guiNode.attachChild(menu);
    }

    private void initGui() {
        initMenu();

        fd = new Picture("fd");
        fd.setImage(app.getAssetManager(), "fd.png", true);
        fd.setWidth(100);
        fd.setHeight(100);
        fd.setLocalTranslation(app.getCamera().getWidth() - 300, app.getCamera().getHeight() - 700 , 0);
        guiNode.attachChild(fd);

        bk = new Picture("bk");
        bk.setImage(app.getAssetManager(), "bk.png", true);
        bk.setWidth(100);
        bk.setHeight(100);
        bk.setLocalTranslation(app.getCamera().getWidth() - 300, app.getCamera().getHeight() - 900 , 0);
        guiNode.attachChild(bk);

        l = new Picture("l");
        l.setImage(app.getAssetManager(), "l.png", true);
        l.setWidth(100);
        l.setHeight(100);
        l.setLocalTranslation(app.getCamera().getWidth() - 400, app.getCamera().getHeight() - 800 , 0);
        guiNode.attachChild(l);

        r = new Picture("r");
        r.setImage(app.getAssetManager(), "r.png", true);
        r.setWidth(100);
        r.setHeight(100);
        r.setLocalTranslation(app.getCamera().getWidth() - 200, app.getCamera().getHeight() - 800 , 0);
        guiNode.attachChild(r);

        space = new Picture("space");
        space.setImage(app.getAssetManager(), "space.png", true);
        space.setWidth(100);
        space.setHeight(100);
        space.setLocalTranslation(app.getCamera().getWidth() - 300, app.getCamera().getHeight() - 800 , 0);
        guiNode.attachChild(space);

        Q = new Picture("Q");
        Q.setImage(app.getAssetManager(), "Q.png", true);
        Q.setWidth(100);
        Q.setHeight(350);
        Q.setLocalTranslation(app.getCamera().getWidth() - 500, app.getCamera().getHeight() - 900 , 0);
        guiNode.attachChild(Q);

        E = new Picture("E");
        E.setImage(app.getAssetManager(), "E.png", true);
        E.setWidth(100);
        E.setHeight(350);
        E.setLocalTranslation(app.getCamera().getWidth() - 100, app.getCamera().getHeight() - 900 , 0);
        guiNode.attachChild(E);
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
        initMenu();
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

            if (name.equals("Click")) {
                float x = app.getInputManager().getCursorPosition().x;
                float y = app.getInputManager().getCursorPosition().y;
                if (Main.inPicture(menu, x, y)) {
                    if (!isMenuOpen && isPressed) openMenu();
                } else if (Main.inPicture(fd, x, y)) {
                    onAction("MoveForward", isPressed, tpf);
                } else if (Main.inPicture(bk, x, y)) {
                    onAction("MoveBackward", isPressed, tpf);
                } else if (Main.inPicture(l, x, y)) {
                    onAction("MoveLeft", isPressed, tpf);
                } else if (Main.inPicture(r, x, y)) {
                    onAction("MoveRight", isPressed, tpf);
                } else if (Main.inPicture(space, x, y)) {
                    onAction("PushBox", isPressed, tpf);
                } else if (Main.inPicture(Q, x, y)) {
                    onAction("RotateLeft", isPressed, tpf);
                } else if (Main.inPicture(E, x, y)) {
                    onAction("RotateRight", isPressed, tpf);
                }
                return;
            }

            if (isPressed) {
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
        menu.removeFromParent();
        fd.removeFromParent();
        bk.removeFromParent();
        l.removeFromParent();
        r.removeFromParent();
        Q.removeFromParent();
        E.removeFromParent();
        space.removeFromParent();
        inputManager.removeListener(actionListener);
        inputManager.clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}