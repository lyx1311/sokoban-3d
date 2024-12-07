package game;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import main.AlertState;
import main.Main;

public class CubeState extends BaseAppState {
    private static final String ARCHIVE_FILE_PATH = "archives/";
    private static final String MAP_FILE_PATH = "assets/maps/";
    private static final String IMAGE_PATH = "images/";
    private static final float EPS = 1e-4f;
    private static final float SIDE = 10f;
    private static final float MOVE_DURATION = 0.8f;
    private static final float ROTATE_DURATION = 1.0f;
    private static final Vector3f UNIT_U = new Vector3f(-1f, 0f, 0f);
    private static final Vector3f UNIT_D = new Vector3f(1f, 0f, 0f);
    private static final Vector3f UNIT_L = new Vector3f(0f, 0f, 1f);
    private static final Vector3f UNIT_R = new Vector3f(0f, 0f, -1f);
    private static final Vector3f pointLightPos = new Vector3f(100, 200, 100);
    private static final Vector3f sunLightDir = new Vector3f(-0.65f, -0.12f, 0.75f);

    private Application app;
    private AssetManager assetManager;
    private FilterState filterState;
    private int level, rows, cols, heroX, heroY;
    private char[][] map = null;
    private Node rootNode = new Node("Scene Root");
    private AmbientLight ambientLight; // 环境光
    private DirectionalLight sunLight; // 太阳光
    private Node cameraNode;
    private CameraControl cameraControl;
    private HashMap<Integer, Geometry> boxes = new HashMap<>();
    private HashSet<Integer> goals = new HashSet<>();
    private String steps = new String();
    private SSAOFilter ssao = new SSAOFilter(7f, 14f, 0.4f, 0.6f); // 屏幕空间环境光遮蔽
    private boolean isWin = false;

    public CubeState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        assetManager = app.getAssetManager();

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }

        // 禁用默认摄像机控制
        ((SimpleApplication) app).getFlyByCamera().setEnabled(false);

        // 创建 cameraNode 并添加 CameraControl
        cameraNode = new Node("Camera Node");
        cameraControl = new CameraControl();
        cameraNode.addControl(cameraControl);
        rootNode.attachChild(cameraNode);

        // 初始化场景
        initSky();
        initFloor();
        initCubes();
        initLights();
        load();
        initCamera();
    }

    private void initSky() {
        Spatial sky = SkyFactory.createSky(
                assetManager,
                "Scenes/Beach/FullskiesSunset0068.dds",
                SkyFactory.EnvMapType.CubeMap);
        sky.setLocalScale(350); // 天空盒大小
        rootNode.attachChild(sky);
    }

    private void initFloor() {
        Material mat = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m"); // 材质

        Quad quad = new Quad(200, 200); // 矩形
        quad.scaleTextureCoordinates(new Vector2f(20, 20)); // 纹理坐标缩放，重复20次

        Geometry geom = new Geometry("Floor", quad); // 几何体
        geom.setMaterial(mat); // 设置材质
        geom.rotate(-FastMath.HALF_PI, 0, 0); // 旋转到水平

        rootNode.attachChild(geom); // 添加到场景
    }

    private void initCubes() {
        File mapFile = new File(MAP_FILE_PATH + level + ".txt");
        try (Scanner sc = new Scanner(mapFile)) {
            rows = sc.nextInt();
            cols = sc.nextInt();
            map = new char[rows][cols];
            heroX = sc.nextInt();
            heroY = sc.nextInt();
            System.out.println("rows: " + rows + ", cols: " + cols + ", heroX: " + heroX + ", heroY: " + heroY);
            sc.nextLine(); // 读取下一行，跳过行列数之后的换行符
            for (int i = 0; i < rows; i++) {
                map[i] = sc.nextLine().toCharArray();
                if (map[i].length != cols) {
                    throw new IllegalArgumentException("Invalid map data: row " + (i + 1) + " has " +
                            map[i].length + " columns (" + map[i] + "), expected " + cols);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < rows; i++) System.out.println(map[i]);

        for (int x = 0; x < rows; x++) for (int y = 0; y < cols; y++) switch (map[x][y]) {
            case 'B': placeBox(x, y); break;
            case '#': placeWall(x, y); break;
            case '.': placeGoal(x, y); map[x][y] = ' '; goals.add(hashId(x, y)); break;
            case 'X': placeBox(x, y); placeGoal(x, y); map[x][y] = 'B'; goals.add(hashId(x, y)); break;
            case ' ': break;
            default: throw new IllegalArgumentException("Invalid map data at (" + x + ", " + y + "): " + map[x][y]);
        }
    }

    private void placeBox(int x, int y) {
        boxes.put(hashId(x, y), placeCube((x + 1) * SIDE * 2, SIDE * 0.6f, -(y + 1) * SIDE * 2,
                SIDE * 0.6f, "Box"));
    }
    private void placeWall(int x, int y) {
        placeCube((x + 1) * SIDE * 2, SIDE, -(y + 1) * SIDE * 2, SIDE, "Wall");
    }
    private void placeGoal(int x, int y) {
        placeGoal((x + 1) * SIDE * 2, SIDE * 0.99f, -(y + 1) * SIDE * 2, SIDE * 0.99f);
    }

    private int hashId(int x, int y) { return x * cols + y; }
    private int hashX(int id) { return id / cols; }
    private int hashY(int id) { return id % cols; }

    private Geometry placeCube(float x, float y, float z, float side, String name) {
        Geometry geom = new Geometry(name, new Box(side, side, side));

        // 加载一张图片作为纹理
        Texture texture = assetManager.loadTexture(getImage(name));

        // 为正方体创建材质并设置纹理
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", texture);
        geom.setMaterial(mat);
        // geom.setMaterial(getMaterial(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f)));

        geom.move(x, y, z);
        rootNode.attachChild(geom);
        return geom;
    }

    private static String getImage(String name) {
        switch (name) {
            case "Wall": return IMAGE_PATH + "wall.bmp";
            case "Box": return IMAGE_PATH + "box.png";
            default: throw new IllegalArgumentException("Invalid image name: " + name);
        }
    }
//    private Material getMaterial(ColorRGBA color) {
//        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.setColor("Diffuse", color);
//        mat.setColor("Ambient", color);
//        mat.setColor("Specular", ColorRGBA.White);
//        mat.setFloat("Shininess", 20f);
//        mat.setBoolean("UseMaterialColors", true);
//        return mat;
//    }
    private void placeGoal(float x, float y, float z, float side) {
        // 创建一个柱体
        Geometry holyLight = new Geometry("HolyLight", new Box(side, 1000, side));

        // 创建材质
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off); // 禁用背面剔除
        material.setColor("Color", new ColorRGBA(1f, 1f, 0f, 0.2f)); // 半透明黄色
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
        holyLight.setMaterial(material); // 设置材质
        holyLight.setQueueBucket(RenderQueue.Bucket.Transparent); // 设置为透明队列

        holyLight.setLocalTranslation(x, 1000, z); // 设置位置
        rootNode.attachChild(holyLight); // 添加到场景

        // 在地面上对应位置添加一个矩形
//        Geometry geom = new Geometry("Goal", new Quad(side * 2, side * 2));
//        Material mat = getMaterial(new ColorRGBA(1f, 1f, 0f, 0.2f)); // 半透明黄色
//        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
//        geom.setMaterial(material); // 设置材质
//        geom.setQueueBucket(RenderQueue.Bucket.Transparent); // 设置为透明队列
//        geom.rotate(-FastMath.HALF_PI, 0, 0);
//        geom.move(x - side, 0.1f, z + side);
//        rootNode.attachChild(geom);
    }

    private void initLights() {
        ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1f));

        sunLight = new DirectionalLight();
        sunLight.setDirection(sunLightDir);
        sunLight.setColor(new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));
    }

    private Vector3f hero() { return new Vector3f((heroX + 1) * SIDE * 2, SIDE, -(heroY + 1) * SIDE * 2); }

    private void initCamera() {
        cameraNode.setLocalTranslation(hero());
        cameraNode.setLocalRotation(new Quaternion(0f, 0.75f, 0f, 0.75f));
    }

    public boolean inMotion() { return cameraControl.isMoving() || cameraControl.isRotating() || cameraControl.isMovingFlyCam(); }
    public boolean isFlying() { return cameraControl.isFlying(); }

    private static boolean isParallel(Vector3f v1, Vector3f v2) {
        return v1.cross(v2).lengthSquared() < EPS && v1.dot(v2) > 0;
    }
    private static char dirToChar(Vector3f direction) {
        System.out.println(direction);
        if (isParallel(direction, UNIT_U)) System.out.println("u");
        if (isParallel(direction, UNIT_D)) System.out.println("d");
        if (isParallel(direction, UNIT_L)) System.out.println("l");
        if (isParallel(direction, UNIT_R)) System.out.println("r");
        if (isParallel(direction, UNIT_U)) return 'u';
        if (isParallel(direction, UNIT_D)) return 'd';
        if (isParallel(direction, UNIT_L)) return 'l';
        if (isParallel(direction, UNIT_R)) return 'r';
        throw new IllegalArgumentException("Invalid direction: " + direction);
    }
    private static Vector3f charToDir(char c) {
        switch (Character.toLowerCase(c)) {
            case 'u': return UNIT_U.clone();
            case 'd': return UNIT_D.clone();
            case 'l': return UNIT_L.clone();
            case 'r': return UNIT_R.clone();
            default: throw new IllegalArgumentException("Invalid direction: " + c);
        }
    }
    private Vector3f strToDir(Vector3f direction, String instruction) {
        switch (instruction) {
            case "MoveForward": return direction;
            case "MoveBackward": return direction.negate();
            case "MoveLeft": return new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y).mult(direction);
            case "MoveRight":return new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y).mult(direction);
            default: throw new IllegalArgumentException("Invalid moving instruction: " + instruction);
        }
    }

    public void moveHero(String instruction) {
        moveHero(strToDir(app.getCamera().getDirection().clone(), instruction), true);
    }
    private void moveHero(Vector3f direction, boolean showAnimation) {
        if (inMotion() || isFlying() || isWin) return;

        direction.multLocal(2 * SIDE);
        Vector3f startPosition = hero();
        Vector3f endPosition = startPosition.add(direction);

        int x = heroX + Math.round(direction.x / (2 * SIDE));
        int y = heroY - Math.round(direction.z / (2 * SIDE));

        // 判断是否可以移动
        if (x < 0 || x >= rows || y < 0 || y >= cols || map[x][y] == '#' || map[x][y] == 'B') return;

        if (showAnimation) cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        // 更新英雄位置
        heroX = x;
        heroY = y;

        // 存档
        if (!checkWin()) steps += dirToChar(direction);
    }

    public void pushBox() { pushBox(app.getCamera().getDirection().clone(), true); }
    private void pushBox(Vector3f direction, boolean showAnimation) {
        if (inMotion() || isFlying() || isWin) return;

        System.out.println("Push box: " + direction);

        direction.multLocal(2 * SIDE);
        Vector3f startPosition = hero();
        Vector3f endPosition = startPosition.add(direction);

        int x = heroX + Math.round(direction.x / (2 * SIDE));
        int y = heroY - Math.round(direction.z / (2 * SIDE));

        // 判断是否可以推箱子
        if (x < 0 || x >= rows || y < 0 || y >= cols || map[x][y] != 'B') return;

        int bx = heroX + Math.round(2 * direction.x / (2 * SIDE));
        int by = heroY - Math.round(2 * direction.z / (2 * SIDE));

        // 判断箱子是否可以移动
        if (bx < 0 || bx >= rows || by < 0 || by >= cols || map[bx][by] == '#' || map[bx][by] == 'B') return;

        // 移动箱子
        if (!boxes.containsKey(hashId(x, y))) {
            throw new IllegalArgumentException("Cube not found at (" + bx + ", " + by + ")");
        } else {
            boxes.get(hashId(x, y)).move(direction);
            boxes.put(hashId(bx, by), boxes.remove(hashId(x, y)));
            System.out.println("Move box: " + x + ", " + y + " -> " + bx + ", " + by);
        }

        // 移动相机
        if (showAnimation) cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        // 更新英雄位置
        heroX = x;
        heroY = y;

        // 更新地图
        map[heroX][heroY] = ' ';
        map[bx][by] = 'B';

        // 存档
        if (!checkWin()) steps += Character.toUpperCase(dirToChar(direction));
    }

    private boolean checkWin() {
        for (int id : goals) {
            int x = hashX(id), y = hashY(id);
            if (map[x][y] != 'B') return false;
        }

        getStateManager().attach(new AlertState(
                "Congratulations!",
                "You have completed level " + level + "!"
        ));

        save();
        isWin = true;

        return true;
    }

    public void undo() {
        if (inMotion() || isFlying() || isWin || steps.isEmpty()) return;

        char c = steps.charAt(steps.length() - 1);
        steps = steps.substring(0, steps.length() - 1);
        Vector3f direction = charToDir(c).negate().mult(2 * SIDE);
        Vector3f startPosition = app.getCamera().getLocation().clone();
        Vector3f endPosition = startPosition.add(direction);

        System.out.println("Undo: " + c + " -> " + direction);

        int x = heroX + Math.round(direction.x / (2 * SIDE));
        int y = heroY - Math.round(direction.z / (2 * SIDE));

        cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        if (Character.isUpperCase(c)) {
            Vector3f boxDirection = charToDir(c).mult(2 * SIDE);
            int bx = heroX + Math.round(boxDirection.x / (2 * SIDE));
            int by = heroY - Math.round(boxDirection.z / (2 * SIDE));

            // 移动箱子
            if (!boxes.containsKey(hashId(bx, by))) {
                throw new IllegalArgumentException("Cube not found at (" + bx + ", " + by + ") when undoing");
            } else {
                boxes.get(hashId(bx, by)).move(direction);
                System.out.println("Move box: " + bx + ", " + by + " -> " + heroX + ", " + heroY);
                boxes.put(hashId(heroX, heroY), boxes.remove(hashId(bx, by)));
            }

            // 更新地图
            map[heroX][heroY] = 'B';
            map[bx][by] = ' ';
        }

        // 更新英雄位置
        heroX = x;
        heroY = y;
    }

    public void rotateCamera(float angle) {
        if (inMotion() || isWin) return;

        // 获取当前相机的水平旋转角度
        Quaternion currentRotation = app.getCamera().getRotation();
        float[] angles = new float[3];
        currentRotation.toAngles(angles);
        float currentYaw = angles[1]; // 获取水平旋转角度（Yaw）

        // 计算新的水平旋转角度
        float newYaw = currentYaw + FastMath.DEG_TO_RAD * angle;

        // 创建新的旋转四元数，仅改变水平旋转角度
        Quaternion newRotation = new Quaternion().fromAngles(angles[0], newYaw, angles[2]);

        // 应用新的旋转角度到相机
        cameraControl.rotateCamera(currentRotation, newRotation, ROTATE_DURATION);
    }

    public void reverseFly() {
        if (inMotion() || isWin) return;
        if (cameraControl.isFlying()) cameraControl.stopFly();
        else cameraControl.startFly();
    }
    public void startMoveFlyCam(String instruction) {
        Vector3f direction = app.getCamera().getDirection().clone();
        direction.y = 0;
        cameraControl.startMoveFlyCam(strToDir(direction, instruction));
    }
    public void stopMoveFlyCam() { cameraControl.stopMoveFlyCam(); }

    public void restart() {
        if (inMotion() || isFlying()) return;

        for (Spatial child : rootNode.getChildren()) {
            if (child instanceof Geometry) rootNode.detachChild(child);
        }
        boxes.clear();
        goals.clear();
        steps = new String();

        initSky();
        initFloor();
        initCubes();
        initCamera();
    }

    public void save() {
        if (inMotion()) return;

        // 打开存档文件
        File archiveFile = new File(ARCHIVE_FILE_PATH + Main.username + "_archive.txt");

        // 读取文件内容到列表
        ArrayList<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(archiveFile)) {
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            // 创建存档文件
            try {
                archiveFile.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // 确保列表至少有 level 行，不足的用空行补齐
        while (lines.size() < level) lines.add("");

        // 修改第k行
        lines.set(level - 1, steps);

        // 将修改后的内容写回文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archiveFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        getStateManager().attach(new AlertState(
                "Game Saved",
                "The game has been saved successfully."
        ));
    }

    public void load() {
        if (Main.username.equals("Visitor")) return;

        // 打开存档文件
        File archiveFile = new File(ARCHIVE_FILE_PATH + Main.username + "_archive.txt");

        // 读取第 level 行
        int linesRead = 0;
        String line = null;
        try (Scanner scanner = new Scanner(archiveFile)) {
            while (scanner.hasNextLine() && linesRead < level) {
                line = scanner.nextLine();
                linesRead++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 恢复游戏状态
        if(linesRead == level) {
            for (char c : line.toCharArray()) {
                if (Character.isLowerCase(c)) moveHero(charToDir(c), false);
                else pushBox(charToDir(c), false);
            }
        }
    }

    @Override
    protected void onEnable() {
        SimpleApplication app = (SimpleApplication) this.app;

        app.getRootNode().attachChild(rootNode);
        app.getRootNode().addLight(ambientLight);
        app.getRootNode().addLight(sunLight);

        filterState = new FilterState();
        filterState.add(ssao);
        getStateManager().attach(filterState);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        // 同步摄像机位置和旋转
        app.getCamera().setLocation(cameraNode.getWorldTranslation());
        app.getCamera().setRotation(cameraNode.getWorldRotation());

        // 检查胜利后的飞行
        if (isWin && !inMotion() && !cameraControl.isFlying()) cameraControl.startFly();
    }

//    @Override
//    public void update(float tpf) {
//        // 移动太阳光
//        float newX = sunLightDir.x + tpf * 1f;
//        float newZ = sunLightDir.z + tpf * 1f;
//        sunLightDir.set(newX, sunLightDir.y, newZ).normalizeLocal();
//        sunLight.setDirection(sunLightDir);
//    }

    @Override
    protected void onDisable() {
        SimpleApplication app = (SimpleApplication) this.app;

        app.getRootNode().detachChild(rootNode);
        app.getRootNode().removeLight(ambientLight);
        app.getRootNode().removeLight(sunLight);

        getStateManager().detach(filterState);
    }

    @Override
    protected void cleanup(Application app) {}
}