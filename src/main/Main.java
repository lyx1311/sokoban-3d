package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
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
        // 设置窗口图标
        //settings.setIcons(new Picture[]{new Picture("images/boxicon.png")});

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
        flyCam.setEnabled(false);

        // 创建背景图片
        Main.createBackground(this);

        // 删除 ESC、F5 键的默认行为
        inputManager.clearMappings();
//        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
//        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);

        // 隐藏左下角的帧率显示和统计信息
        setDisplayFps(false);
        setDisplayStatView(false);
    }

    private static Geometry cubeGeo;
    private static DirectionalLight cubeLight;
    public static void createBackground(Application app) {
        Mesh cube = new Box(1, 1, 1);
        cubeGeo = new Geometry("Cube", cube);
        ((SimpleApplication) app).getRootNode().attachChild(cubeGeo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("images/box - interface.png"));
        cubeGeo.setMaterial(mat);
        cubeLight = new DirectionalLight();
        cubeLight.setDirection(new Vector3f(-1, -2, -3));
        ((SimpleApplication) app).getRootNode().addLight(cubeLight);
        app.getCamera().setLocation(new Vector3f(2.98f, 4.98f, 7.19f));
        app.getCamera().setRotation(new Quaternion(-0.08f, 0.92f, -0.27f, -0.26f));
    }
    public static void removeBackground(Application app) {
        if (cubeGeo != null) {
            cubeGeo.removeFromParent();
            cubeGeo = null;

            if (app instanceof SimpleApplication) {
                ((SimpleApplication) app).getRootNode().removeLight(cubeLight);
            } else {
                throw new IllegalStateException("Application is not an instance of SimpleApplication");
            }
        } else {
            throw new IllegalStateException("Background not found");
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (cubeGeo != null) {
            // 旋转速度：每秒360°
            float speed = FastMath.HALF_PI;
            // 让方块匀速旋转
            cubeGeo.rotate(0, tpf * speed, 0);
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