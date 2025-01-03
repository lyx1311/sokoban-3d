package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.RawInputListener;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

public class AlertState extends BaseAppState {
    private static final float deltaX = 200; // 弹框相对于屏幕中央的水平偏移
    private static final float startHeight = 200; // 弹框相对于屏幕中央的初始高度
    private static final float endHeight = 300; // 弹框相对于屏幕中央的最终高度
    private static final float stopMovingTime = 0.8f; // 弹框从初始高度到最终高度的时间（秒）
    private static final float maxTime = 3.0f; // 自动关闭的时间（秒）
    private static final float interval = 60; // 弹框之间的间隔

    private static boolean locationUsed[] = new boolean[1000]; // 用于记录弹框位置是否被占用

    private Application app;
    private String title;
    private String message;
    private Container alertBox;
    private Node guiNode;
    private Geometry background; // 全屏背景遮挡层
    private RawInputListener inputInterceptor; // 用于监测鼠标输入
    private int location = 0; // 弹框是第几个
    private float timer = 0;  // 用于计时
    private Label titleLabel, messageLabel;

    public AlertState(String title, String message) {
        this.title = title;
        this.message = message;

        // 寻找第一个未被占用的位置
        while (locationUsed[location]) location++;
        locationUsed[location] = true;
    }

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

        // 创建全屏背景遮挡层
        createBackground(app);

        // 创建弹框容器
        createAlertBox();
    }

    private void createBackground(Application app) {
        float width = app.getCamera().getWidth();
        float height = app.getCamera().getHeight();

        // 创建一个全屏矩形
        Quad quad = new Quad(width, height);
        background = new Geometry("AlertBackground", quad);

        // 设置材质，使用半透明颜色
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        // 设置半透明颜色
        mat.setColor("Color", new com.jme3.math.ColorRGBA(0, 0, 0, 0.5f));

        // 启用透明模式
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        // 确保正确的深度测试和排序
        background.setQueueBucket(RenderQueue.Bucket.Transparent);
        background.setMaterial(mat);

        // 设置背景位置，确保它在 GUI 层的底部
        background.setLocalTranslation(0, 0, 0);
    }

    private void createAlertBox() {
        alertBox = new Container();

        // 添加标题
        titleLabel = alertBox.addChild(new Label(title));
        titleLabel.setFontSize(24);
        titleLabel.setColor(new com.jme3.math.ColorRGBA(1, 1, 1, 1));

        // 添加消息内容
        messageLabel = alertBox.addChild(new Label(message));
        messageLabel.setFontSize(18);
        messageLabel.setColor(new com.jme3.math.ColorRGBA(1, 1, 1, 1));

        // 设置弹框位置
        alertBox.setLocalTranslation(
                app.getCamera().getWidth() / 2f - deltaX,
                app.getCamera().getHeight() / 2f + startHeight - location * interval,
                1
        );
    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(background); // 添加全屏背景
        guiNode.attachChild(alertBox); // 添加弹框

        // 激活输入监听
        InputManager inputManager = app.getInputManager();
        if (inputInterceptor == null) {
            inputInterceptor = new RawInputListener() {
                @Override
                public void onMouseButtonEvent(com.jme3.input.event.MouseButtonEvent evt) {
                    if (evt.isPressed()) onDisable(); // 点击任意位置关闭弹框
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
    public void update(float tpf) {
        timer += tpf;

        if (timer >= maxTime) {
            onDisable(); // 超过最大时间后关闭
            cleanup(); // 清理资源
        } else if (timer <= stopMovingTime) {
            // 计算弹框的高度
            float height = startHeight - location * interval + (endHeight - startHeight) * Math.min(timer / stopMovingTime, 1);

            // 设置弹框位置
            alertBox.setLocalTranslation(
                    app.getCamera().getWidth() / 2f - deltaX,
                    app.getCamera().getHeight() / 2f + height,
                    1
            );
        } else {
            alertBox.setLocalTranslation(
                    app.getCamera().getWidth() / 2f - deltaX,
                    app.getCamera().getHeight() / 2f + endHeight - location * interval,
                    1
            );
        }
    }

    @Override
    protected void onDisable() {
        background.removeFromParent(); // 移除全屏背景
        alertBox.removeFromParent(); // 移除弹框
        app.getInputManager().removeRawInputListener(inputInterceptor); // 移除输入监听
    }

    @Override
    protected void cleanup(Application app) {
        locationUsed[location] = false; // 释放位置
    }
}