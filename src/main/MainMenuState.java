package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
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
    private Container menu;
    private Picture backgroundImage;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application is not an instance of SimpleApplication");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();

        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        initGui();

        // 创建全屏背景遮挡层
        createBackground();
    }

    private void initGui() {
        menu = new Container();
        Button loginButton = menu.addChild(new Button("Log In"));
        loginButton.setFontSize(24);
        Button registerButton = menu.addChild(new Button("Register"));
        registerButton.setFontSize(24);
        Button exitButton = menu.addChild(new Button("Exit"));
        exitButton.setFontSize(24);

        // 设置按钮点击事件
        loginButton.addClickCommands(source -> {
            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new LoginState()); // 切换到登录状态
        });
        registerButton.addClickCommands(source -> {
            getStateManager().detach(this); // 移除当前状态
            getStateManager().attach(new RegisterState()); // 切换到注册状态
        });
        exitButton.addClickCommands(source -> {
            app.stop(); // 退出游戏
        });

        // 设置窗口位置
        menu.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
    }

    private void createBackground() {
        float width = app.getCamera().getWidth();
        float height = app.getCamera().getHeight();

        // 创建 Picture 对象
        backgroundImage = new Picture("Background");
        backgroundImage.setImage(app.getAssetManager(), "bg.jpg", false); // 替换为你的图片路径
        backgroundImage.setWidth(width);
        backgroundImage.setHeight(height);

        // 设置位置，确保背景图片在最底层
        backgroundImage.setLocalTranslation(0, 0, 0);

        // 将背景添加到 GUI 节点
        guiNode.attachChild(backgroundImage);
    }

    @Override
    public void onEnable() {
        guiNode.attachChild(menu); // 将菜单添加到 GUI 节点
    }

    @Override
    public void onDisable() {
        for (Spatial child : menu.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        menu.removeFromParent(); // 将菜单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}