package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;

public class SettingState extends BaseAppState {
    private static float moveSpeed = 0.8f;
    private static float rotateSpeed = 1.5f;
    private static int solverTimeLimit = 10;

    private Application app;
    private Node guiNode;
    private Container container;
    private Picture back, about, settings;
    private Label moveSpeedLabel, rotateSpeedLabel, solverTimeLimitLabel;
    private Button apply;

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

    public static float getMoveSpeed() { return moveSpeed; }
    public static float getRotateSpeed() { return rotateSpeed; }
    public static int getSolverTimeLimit() { return solverTimeLimit; }

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
                if (Main.inPicture(back, x, y)) {
                    Main.playClickSound();
                    getStateManager().detach(SettingState.this); // 移除当前状态
                    getStateManager().attach(new LevelSelectionState()); // 切换到登
                } else if (Main.inPicture(about, x, y) && !Main.username.equals("Visitor")) {
                    Main.playCollectSound();
                    Main.addMoney();
                }
            }
        }
    };

    private void initGui() {
        container = new Container();
        guiNode.attachChild(container);

        // 添加标题
        container.addChild(new Label("               ")).setFontSize(50);

        settings = new Picture("settings");
        settings.setImage(app.getAssetManager(), "buttonsettings.png", true);
        settings.setWidth(263);
        settings.setHeight(70);
        settings.setLocalTranslation(12, app.getCamera().getHeight()-80 , 0);
        guiNode.attachChild(settings);

        // 添加设置表单
        Container settingForm = new Container();
        container.addChild(settingForm);

        // 添加返回按钮
        /*Button backButton = container.addChild(new Button("Back"));
        backButton.setFontSize(24);
        backButton.addClickCommands(source -> {
            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new LevelSelectionState()); // 切换到 LevelSelectionState
        });*/

        back = new Picture("back");
        back.setImage(app.getAssetManager(), "buttonback.png", true);
        back.setWidth(193);
        back.setHeight(60);
        back.setLocalTranslation(20, app.getCamera().getHeight() - 300 , 0);
        guiNode.attachChild(back);

        about = new Picture("about");
        about.setImage(app.getAssetManager(), "announcement.png", true);
        about.setWidth(535);
        about.setHeight(214);
        about.setLocalTranslation(20, app.getCamera().getHeight() - 700 , 0);
        guiNode.attachChild(about);

        // 移动速度设置
        moveSpeedLabel=settingForm.addChild(new Label("Move Speed: " + String.format("%.2f", moveSpeed) +
                " seconds per move"));
        moveSpeedLabel.setFontSize(24);
        moveSpeedLabel.setColor(ColorRGBA.White);
        Slider moveSpeedSlider = settingForm.addChild(new Slider(new DefaultRangedValueModel(0.1f, 1.5f,
                moveSpeed)), 1);
        setStyle(moveSpeedSlider, 0.1f);
        addButton(settingForm).addClickCommands(source -> {
            Main.playClickSound();
            moveSpeed = Math.round(moveSpeedSlider.getModel().getValue() * 100) / 100.0f;

            onDisable();
            onEnable();
        });

        // 旋转速度设置
        rotateSpeedLabel=settingForm.addChild(new Label("Rotate Speed: " + String.format("%.2f", rotateSpeed) +
                " seconds per rotation"));
        rotateSpeedLabel.setFontSize(24);
        rotateSpeedLabel.setColor(ColorRGBA.White);
        Slider rotateSpeedSlider = settingForm.addChild(new Slider(new DefaultRangedValueModel(0.1f, 3.0f,
                rotateSpeed)), 1);
        setStyle(rotateSpeedSlider, 0.1f);
        addButton(settingForm).addClickCommands(source -> {
            Main.playClickSound();
            rotateSpeed = Math.round(rotateSpeedSlider.getModel().getValue() * 100) / 100.0f;

            onDisable();
            onEnable();
        });

        // 求解器时间限制设置
        solverTimeLimitLabel=settingForm.addChild(new Label("Solver Time Limit: " + String.format("%d", solverTimeLimit) +
                " seconds"));
        solverTimeLimitLabel.setFontSize(24);
        solverTimeLimitLabel.setColor(ColorRGBA.White);
        Slider solverTimeLimitSlider = settingForm.addChild(new Slider(new DefaultRangedValueModel(1.0f, 30.0f,
                solverTimeLimit)), 1);
        setStyle(solverTimeLimitSlider, 1.0f);
        addButton(settingForm).addClickCommands(source -> {
            Main.playClickSound();
            solverTimeLimit = (int) Math.round(solverTimeLimitSlider.getModel().getValue());

            onDisable();
            onEnable();
        });

        // 设置窗口位置
        container.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
    }

    private void setStyle(Slider slider, float delta) {
        slider.setPreferredSize(new Vector3f(200, 32, 1));
        slider.getThumbButton().setSize(new Vector3f(12, 32, 1));
        slider.getThumbButton().setFontSize(20);
        slider.getDecrementButton().setFontSize(24);
        slider.getIncrementButton().setFontSize(24);
        slider.setDelta(delta);
    }

    private Button addButton(Container container) {
        apply = container.addChild(new Button("Apply"), 2);
        apply.setColor(ColorRGBA.White);
        apply.setFontSize(24);
        return apply;
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        guiNode.attachChild(container); // 将菜单添加到 GUI 节点
        initInput();
    }

    @Override
    public void onDisable() {
        for (Spatial child : container.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
            if (child instanceof Container) {
                for (Spatial grandchild : ((Container) child).getChildren()) {
                    if (grandchild instanceof Button) ((Button) grandchild).setEnabled(false); // 禁用按钮
                }
            }
        }
        back.removeFromParent();
        about.removeFromParent();
        settings.removeFromParent();
        container.detachAllChildren(); // 移除所有子节点
        container.removeFromParent(); // 将菜单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}