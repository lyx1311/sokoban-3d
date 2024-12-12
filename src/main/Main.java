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
        settings.setResolution(1600, 900); // 设置分辨率为屏幕分辨率
        settings.setResizable(true); // 缩放窗口
        settings.setTitle("SOKOBAN!"); // 设置窗口标题

        // 启动程序
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();

        // 移除空格键的默认映射
        app.getInputManager().deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new MainMenuState());

        // 初始化相机
        flyCam.setMoveSpeed(50f);
        flyCam.setDragToRotate(false);

        // 创建背景图片
        Main.createBackground(this);
        //使背景图片适应屏幕大小
        viewPort.setBackgroundColor(new com.jme3.math.ColorRGBA(0.7f, 0.8f, 1f, 1f));
     }

    private static Picture background = null;
    public static void createBackground(Application app) {
        float width = app.getCamera().getWidth();
        float height = app.getCamera().getHeight();

        // 创建 Picture 对象
        background = new Picture("Background");
        background.setImage(app.getAssetManager(), "interface.jpg", true); // 替换为你的图片路径
        background.setWidth(width);
        background.setHeight(height);
        float x = 0.5f * (app.getCamera().getWidth() - width);
        float y = 0.5f * (app.getCamera().getHeight() - height);
        // 设置位置，确保背景图片在最底层
        background.setLocalTranslation(x, y, -1);

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

    public static boolean inPicture(Picture p, float x, float y) {
        float width = p.getWidth();
        float height = p.getHeight();
        float x1 = p.getLocalTranslation().x;
        float y1 = p.getLocalTranslation().y;
        return x >= x1 && x <= x1 + width && y >= y1 && y <= y1 + height;
    }
}