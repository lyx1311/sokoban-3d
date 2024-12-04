package game;

import java.io.File;
import java.io.FileNotFoundException;
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

public class CubeState extends BaseAppState {
    private static final String MAP_FILE_PATH = "assets/maps/";
    private static final String IMAGE_PATH = "images/";
    private static final float EPS = 1e-3f;
    private static final float SIDE = 10f;
    private static final float MOVE_DURATION = 1.0f;
    private static final float ROTATE_DURATION = 0.5f;
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
    private HashSet<Integer> goals = new HashSet<>();
    private Node rootNode = new Node("Scene Root");
    private AmbientLight ambientLight; // 环境光
    private PointLight pointLight; // 点光源
    private DirectionalLight sunLight; // 太阳光
    private Node cameraNode;
    private CameraControl cameraControl;
    private HashMap<Integer, Geometry> cubes = new HashMap<>();
    private String steps = new String();
    private SSAOFilter ssao = new SSAOFilter(7f, 14f, 0.4f, 0.6f); // 屏幕空间环境光遮蔽

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
        initFloor();
        initCubes();
        initLights();
        initSky();
        initCamera();
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
        cubes.put(hashId(x, y), placeCube((x + 1) * SIDE * 2, SIDE * 0.6f, -(y + 1) * SIDE * 2,
                SIDE * 0.6f, "Box"));
    }
    private void placeWall(int x, int y) {
        placeCube((x + 1) * SIDE * 2, SIDE, -(y + 1) * SIDE * 2, SIDE, "Wall");
    }
    private void placeGoal(int x, int y) {
        placeGoal((x + 1) * SIDE * 2, -(y + 1) * SIDE * 2);
    }

    private int hashId(int x, int y) { return x * cols + y; }
    // private int hashX(int id) { return id / cols; }
    // private int hashY(int id) { return id % cols; }

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

    private void placeGoal(float x, float z) {
        // 创建一个圆柱体
        Geometry holyLight = new Geometry("HolyLight", new Box(SIDE, 1000, SIDE));

        // 创建材质
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", new ColorRGBA(1f, 1f, 0f, 0.2f)); // 半透明黄色
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
        holyLight.setMaterial(material); // 设置材质
        holyLight.setQueueBucket(RenderQueue.Bucket.Transparent); // 设置为透明队列

        holyLight.setLocalTranslation(x, 1000, z); // 设置位置
        rootNode.attachChild(holyLight); // 添加到场景

        // 在地面上对应位置添加一个矩形
        Geometry geom = new Geometry("Goal", new Quad(SIDE * 2, SIDE * 2));
        Material mat = getMaterial(new ColorRGBA(1f, 1f, 0f, 0.2f)); // 半透明黄色
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
        geom.setMaterial(material); // 设置材质
        geom.rotate(-FastMath.HALF_PI, 0, 0);
        geom.move(x - SIDE, 0.01f, z + SIDE);
        rootNode.attachChild(geom);
    }

    private void initLights() {
        ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1f));

        pointLight = new PointLight();
        pointLight.setPosition(pointLightPos);
        pointLight.setRadius(1000);

        sunLight = new DirectionalLight();
        sunLight.setDirection(sunLightDir);
        sunLight.setColor(new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));
    }

    private void initSky() {
        Spatial sky = SkyFactory.createSky(
                assetManager,
                "Scenes/Beach/FullskiesSunset0068.dds",
                SkyFactory.EnvMapType.CubeMap);
        sky.setLocalScale(350); // 天空盒大小
        rootNode.attachChild(sky);
    }

    private void initCamera() {
        SimpleApplication app = (SimpleApplication) getApplication();
        Vector3f cameraPosition = new Vector3f((heroX + 1) * SIDE * 2, SIDE, -(heroY + 1) * SIDE * 2);
        Quaternion cameraRotation = new Quaternion(0f, 0.75f, 0f, 0.75f);

        cameraNode.setLocalTranslation(cameraPosition);
        cameraNode.setLocalRotation(cameraRotation);

        app.getCamera().setLocation(cameraPosition);
        app.getCamera().setRotation(cameraRotation);
    }

    public boolean inMotion() { return cameraControl.isMoving() || cameraControl.isRotating(); }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        // 同步摄像机位置和旋转
        app.getCamera().setLocation(cameraNode.getWorldTranslation());
        app.getCamera().setRotation(cameraNode.getWorldRotation());
    }

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
            case 'u': return UNIT_U;
            case 'd': return UNIT_D;
            case 'l': return UNIT_L;
            case 'r': return UNIT_R;
            default: throw new IllegalArgumentException("Invalid direction: " + c);
        }
    }

    public void move(String instruction) {
        Vector3f cameraDirection = app.getCamera().getDirection().clone().multLocal(2 * SIDE);
        Vector3f direction;
        switch (instruction) {
            case "MoveForward":
                direction = cameraDirection;
                break;
            case "MoveBackward":
                direction = cameraDirection.negate();
                break;
            case "MoveLeft":
                direction = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y).mult(cameraDirection);
                break;
            case "MoveRight":
                direction = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y).mult(cameraDirection);
                break;
            default:
                throw new IllegalArgumentException("Invalid moving instruction: " + instruction);
        }

        Vector3f startPosition = app.getCamera().getLocation().clone();
        Vector3f endPosition = startPosition.add(direction);

        int x = heroX + Math.round(direction.x / (2 * SIDE));
        int y = heroY - Math.round(direction.z / (2 * SIDE));

        // 判断是否可以移动
        if (x < 0 || x >= rows || y < 0 || y >= cols || map[x][y] == '#' || map[x][y] == 'B') return;

        cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        // 更新英雄位置
        heroX = x;
        heroY = y;

        // 存档
        steps += dirToChar(direction);
    }

    public void pushBox() {
        Vector3f direction = app.getCamera().getDirection().clone().multLocal(2 * SIDE);
        Vector3f startPosition = app.getCamera().getLocation().clone();
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
        if (!cubes.containsKey(hashId(x, y))) {
            throw new IllegalArgumentException("Cube not found at (" + bx + ", " + by + ")");
        } else {
            cubes.get(hashId(x, y)).move(direction);
            cubes.put(hashId(bx, by), cubes.remove(hashId(x, y)));
        }

        // 移动相机
        cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        // 更新英雄位置
        heroX = x;
        heroY = y;

        // 更新地图
        map[heroX][heroY] = ' ';
        map[bx][by] = 'B';

        // 存档
        steps += Character.toUpperCase(dirToChar(direction));
    }

    public void undo() {
        if (steps.isEmpty()) return;

        char c = steps.charAt(steps.length() - 1);
        steps = steps.substring(0, steps.length() - 1);
        Vector3f direction = charToDir(c).negate().multLocal(2 * SIDE);
        Vector3f startPosition = app.getCamera().getLocation().clone();
        Vector3f endPosition = startPosition.add(direction);

        System.out.println("Undo: " + c + " -> " + direction);

        int x = heroX + Math.round(direction.x / (2 * SIDE));
        int y = heroY - Math.round(direction.z / (2 * SIDE));

        cameraControl.moveCamera(startPosition, endPosition, MOVE_DURATION);

        if (Character.isUpperCase(c)) {
            Vector3f boxDirection = charToDir(c).multLocal(2 * SIDE);
            int bx = heroX + Math.round(boxDirection.x / (2 * SIDE));
            int by = heroY - Math.round(boxDirection.z / (2 * SIDE));

            // 移动箱子
            if (!cubes.containsKey(hashId(bx, by))) {
                throw new IllegalArgumentException("Cube not found at (" + bx + ", " + by + ") when undoing");
            } else {
                cubes.get(hashId(bx, by)).move(direction.negate());
                cubes.put(hashId(x, y), cubes.remove(hashId(bx, by)));
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
        Quaternion startRotation = app.getCamera().getRotation().clone();
        Quaternion endRotation = startRotation.mult(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * angle, Vector3f.UNIT_Y));

        cameraControl.rotateCamera(startRotation, endRotation, ROTATE_DURATION);
    }

    private Material getMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 20f);
        mat.setBoolean("UseMaterialColors", true);
        return mat;
    }

    @Override
    protected void onEnable() {
        SimpleApplication app = (SimpleApplication) getApplication();

        app.getRootNode().attachChild(rootNode);
        app.getRootNode().addLight(ambientLight);
        // app.getRootNode().addLight(pointLight);
        app.getRootNode().addLight(sunLight);

        filterState = new FilterState();
        filterState.add(ssao);
        getStateManager().attach(filterState);
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
        SimpleApplication app = (SimpleApplication) getApplication();

        app.getRootNode().detachChild(rootNode);
        app.getRootNode().removeLight(ambientLight);
        app.getRootNode().removeLight(pointLight);
        app.getRootNode().removeLight(sunLight);

        getStateManager().detach(filterState);
    }

    @Override
    protected void cleanup(Application app) {}
}
