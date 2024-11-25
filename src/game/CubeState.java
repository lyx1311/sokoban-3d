package game;

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
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CubeState extends BaseAppState {
    private static final String MAP_FILE_PATH = "assets/maps/";
    private static final String IMAGE_PATH = "images/";
    private static final float side = 10f;

    private Application app;
    private int level, rows, cols, heroX, heroY;
    private Node rootNode = new Node("Scene Root");
    private AmbientLight ambientLight; // 环境光
    private PointLight pointLight; // 点光源
    private final Vector3f pointLightPos = new Vector3f(100, 200, 100);
    private DirectionalLight sunLight; // 太阳光
    private final Vector3f sunLightDir = new Vector3f(-0.65f, -0.12f, 0.75f);
    private AssetManager assetManager;

    public CubeState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        assetManager = app.getAssetManager();
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
        ArrayList<String> mapData = new ArrayList<>();
        File mapFile = new File(MAP_FILE_PATH + level + ".txt");
        try (Scanner sc = new Scanner(mapFile)) {
            rows = sc.nextInt();
            cols = sc.nextInt();
            heroX = sc.nextInt();
            heroY = sc.nextInt();
            System.out.println("rows: " + rows + ", cols: " + cols + ", heroX: " + heroX + ", heroY: " + heroY);
            sc.nextLine(); // 读取下一行，跳过行列数之后的换行符
            for (int i = 0; i < rows; i++) {
                mapData.add(sc.nextLine());
                if (mapData.get(i).length() != cols) {
                    throw new IllegalArgumentException("Invalid map data: row " + (i + 1) + " has " +
                            mapData.get(i).length() + " columns (" + mapData.get(i) + "), expected " + cols);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(mapData);

        for (int x = 0; x < rows; x++) for (int y = 0; y < cols; y++) switch (mapData.get(x).charAt(y)) {
            case 'B': placeCube((x + 1) * side * 2, -(y + 1) * side * 2, "Cube"); break;
            case '#': placeCube((x + 1) * side * 2, -(y + 1) * side * 2, "Wall"); break;
            case '.': placeGoal((x + 1) * side * 2, -(y + 1) * side * 2); break;
        }
    }

    private void placeCube(float x, float z, String name) {
        Geometry geom = new Geometry(name, new Box(side, side, side));

        // 加载一张图片作为纹理
        Texture texture = assetManager.loadTexture(getImage(name));

        // 为正方体创建材质并设置纹理
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", texture);
        geom.setMaterial(mat);
//        geom.setMaterial(getMaterial(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f)));

        geom.move(x, side, z);
        rootNode.attachChild(geom);
    }

    private static String getImage(String name) {
        switch (name) {
            case "Wall": return IMAGE_PATH + "wall.bmp";
            case "Cube": return IMAGE_PATH + "box.png";
            default: throw new IllegalArgumentException("Invalid image name: " + name);
        }
    }

    private void placeGoal(float x, float z) {
//        float height = 1000f; // 柱体高度
//
//        Material sideMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        sideMaterial.setTexture("ColorMap", assetManager.loadTexture("Textures/gradients/yellow_to_white.png")); // 使用渐变纹理
//        sideMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
//
//        // 前面
//        Geometry front = new Geometry("FrontFace", new Quad(side * 2, height));
//        front.setMaterial(sideMaterial);
//        front.rotate(0, 0, 0); // 无需旋转
//        front.move(x - side, 0, z + side); // 前面矩形
//        rootNode.attachChild(front);
//
//        // 背面
//        Geometry back = new Geometry("BackFace", new Quad(side * 2, height));
//        back.setMaterial(sideMaterial);
//        back.rotate(0, FastMath.PI, 0); // 反向显示
//        back.move(x + side, 0, z - side); // 背面矩形
//        rootNode.attachChild(back);
//
//        // 左侧面
//        Geometry left = new Geometry("LeftFace", new Quad(side * 2, height));
//        left.setMaterial(sideMaterial);
//        left.rotate(0, -FastMath.HALF_PI, 0); // 左侧矩形
//        left.move(x - side, 0, z - side); // 位于左边
//        rootNode.attachChild(left);
//
//        // 右侧面
//        Geometry right = new Geometry("RightFace", new Quad(side * 2, height));
//        right.setMaterial(sideMaterial);
//        right.rotate(0, FastMath.HALF_PI, 0); // 右侧矩形
//        right.move(x + side, 0, z + side); // 位于右边
//        rootNode.attachChild(right);

        // 在地面上对应位置添加一个矩形
        Geometry ground = new Geometry("Ground", new Quad(side * 2, side * 2));
        Material groundMaterial = getMaterial(new ColorRGBA(1f, 1f, 0f, 0.2f)); // 半透明黄色
        groundMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha); // 启用透明
        ground.setMaterial(groundMaterial); // 设置材质
        ground.rotate(-FastMath.HALF_PI, 0, 0); // 平放在地面
        ground.move(x - side, 0.01f, z + side); // 设置地面矩形位置
        rootNode.attachChild(ground);
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
        app.getCamera().setLocation(new Vector3f((heroX * 2 + 1) * side, 5f, -(heroY * 2 + 1) * side));
        app.getCamera().setRotation(new Quaternion(0f, -0.75f, 0f, 0.75f));
        System.out.println(app.getCamera().getLocation());
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
//        app.getRootNode().addLight(pointLight);
        app.getRootNode().addLight(sunLight);
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
    }

    @Override
    protected void cleanup(Application app) {}
}
