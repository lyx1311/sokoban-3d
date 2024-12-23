package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
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

import java.io.*;
import java.util.Scanner;

public class Main extends SimpleApplication {
    public static final int SOLVER_COST = 150;
    public static final int SOLVE_GAIN = 100;
    public static final char UNSOLVED = '0';
    public static final char SOLVED = '1';
    public static final char SOLVER_BOUGHT = '2';

    private static Main app;
    public static String username = "";

    public static void main(String[] args) {
        app = new Main();

        // 设置 AppSettings
        AppSettings settings = new AppSettings(true);
        settings.setFullscreen(false); // 设置为窗口模式
        settings.setResolution(1600, 900); // 设置分辨率为屏幕分辨率
        settings.setResizable(false); // 缩放窗口
        settings.setTitle("Sokoban Monkey"); // 设置窗口标题

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

    @Override
    public void simpleUpdate(float tpf) {
        if (cubeGeo != null) {
            // 旋转速度：每秒 90°
            float speed = FastMath.HALF_PI;
            // 让方块匀速旋转
            cubeGeo.rotate(0, tpf * speed, 0);
        }
    }

    private static Geometry cubeGeo;
    private static DirectionalLight cubeLight;
    private static AudioNode backgroundMusic;
    public static void createBackground(Application app) {
        Mesh cube = new Box(1.5f, 1.5f, 1.5f);
        cubeGeo = new Geometry("Cube", cube);
        ((SimpleApplication) app).getRootNode().attachChild(cubeGeo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("images/box - interface.png"));
        cubeGeo.setMaterial(mat);
        cubeLight = new DirectionalLight();
        cubeLight.setDirection(new Vector3f(-1, -2, -3));
        ((SimpleApplication) app).getRootNode().addLight(cubeLight);
        app.getCamera().setLocation(new Vector3f(2.78f, 4.98f, 7.19f));
        app.getCamera().setRotation(new Quaternion(-0.08f, 0.92f, -0.27f, -0.26f));

        if (backgroundMusic != null) backgroundMusic.stop();

        backgroundMusic = new AudioNode(app.getAssetManager(), "sounds/Grasswalk.wav", false);
        backgroundMusic.setPositional(false);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
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

        if (backgroundMusic != null) {
            backgroundMusic.stop();

            backgroundMusic = new AudioNode(app.getAssetManager(), "sounds/Riddle.wav", false);
            backgroundMusic.setPositional(false);
            backgroundMusic.setLooping(true);
            backgroundMusic.play();
        } else {
            throw new IllegalStateException("Background music not found");
        }
    }

    public static void playClickSound() { playSound("sounds/Click.wav"); }
    public static void playWinSound() { playSound("sounds/Win.wav"); }
    public static void playLoseSound() { playSound("sounds/Lose.wav"); }
    public static void playPushSound() { playSound("sounds/Push.wav"); }
    private static void playSound(String sound) {
        AudioNode audio = new AudioNode(app.getAssetManager(), sound, false);
        audio.setPositional(false);
        audio.setLooping(false);
        audio.play();
    }

    public static int getMoney() {
        File file = new File("archives/" + username + "_status.txt");
        try (Scanner sc = new Scanner(file)) {
            return sc.nextInt();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Status file of user " + username + " not found!");
        }
    }
    private static String getStatus() {
        File file = new File("archives/" + username + "_status.txt");
        try (Scanner sc = new Scanner(file)) {
            sc.nextInt();
            return sc.next();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Status file of user " + username + " not found!");
        }
    }
    public static boolean buySolver(int level) {
        int money = getMoney();
        String status = getStatus();
        if (money >= SOLVER_COST) {
            File file = new File("archives/" + username + "_status.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                status = status.substring(0, level - 1) + SOLVER_BOUGHT + status.substring(level);
                writer.write(money - SOLVER_COST + "\n" + status);
                app.getStateManager().attach(new AlertState(
                        "Solver Bought",
                        "Successfully bought the solver for level " + level + " at the cost of $" + SOLVER_COST + "."
                ));
                return true;
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to write to status file of user " + username);
            }
        } else {
            return false;
        }
    }
    public static char getLevelStatus(int level) { return getStatus().charAt(level - 1); }
    public static void solveLevel(int level) {
        int money = getMoney();
        String status = getStatus();
        File file = new File("archives/" + username + "_status.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            status = status.substring(0, level - 1) + SOLVED + status.substring(level);
            writer.write(money + SOLVE_GAIN + "\n" +status);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write to status file of user " + username);
        }
    }

    public static boolean inPicture(Picture p, float x, float y) {
        if (p == null) return false;
        float width = p.getWidth();
        float height = p.getHeight();
        float x1 = p.getLocalTranslation().x;
        float y1 = p.getLocalTranslation().y;
        return x >= x1 && x <= x1 + width && y >= y1 && y <= y1 + height;
    }
}