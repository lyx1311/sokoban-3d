package main;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

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
}