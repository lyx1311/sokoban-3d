package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

public class Main extends SimpleApplication {
    public static String username = "";

    public static void main(String[] args) {
        Main app = new Main();

        // 设置 AppSettings
        AppSettings settings = new AppSettings(true);
        settings.setFullscreen(false); // 设置为窗口模式
        settings.setResolution(1280, 720); // 设置分辨率为屏幕分辨率
        settings.setResizable(false); // 禁止缩放窗口
        settings.setTitle("Hello, World!"); // 设置窗口标题

        // 启动程序
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new MainMenuState());

        // 初始化相机
        flyCam.setMoveSpeed(50f);
        flyCam.setDragToRotate(true);
    }

    private static Picture background;
    public static void createBackground(Application app) {
        float width = app.getCamera().getWidth();
        float height = app.getCamera().getHeight();

        // 创建 Picture 对象
        background = new Picture("Background");
        background.setImage(app.getAssetManager(), "bg.jpg", false); // 替换为你的图片路径
        background.setWidth(width);
        background.setHeight(height);

        // 设置位置，确保背景图片在最底层
        background.setLocalTranslation(0, 0, -1);

        // 将背景添加到 GUI 节点
        if (app instanceof SimpleApplication) {
            ((SimpleApplication) app).getGuiNode().attachChild(background);
        } else {
            throw new IllegalArgumentException("Application is not an instance of SimpleApplication");
        }
    }
    public static void removeBackground() {
        if (background != null) {
            background.removeFromParent();
        } else {
            throw new IllegalStateException("Background not found");
        }
    }
}