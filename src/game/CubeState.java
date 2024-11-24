package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.util.SkyFactory;

public class CubeState extends BaseAppState {

    private Node root = new Node("Scene Root");
    private AmbientLight ambientLight; // 环境光
    private PointLight pointLight; // 点光源
    private final Vector3f pointLightPos = new Vector3f(100, 200, 100);
    private DirectionalLight sunLight; // 太阳光
    private final Vector3f sunLightDir = new Vector3f(-0.65f, -0.12f, 0.75f);
    private AssetManager assetManager;

    @Override
    protected void initialize(Application app) {
        assetManager = app.getAssetManager();
        initFloor();
        initCubes();
        initLights();
        initSky();
    }

    private void initFloor() {
        Material mat = assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m"); // 材质

        Quad quad = new Quad(200, 200); // 矩形
        quad.scaleTextureCoordinates(new Vector2f(20, 20)); // 纹理坐标缩放，重复20次

        Geometry geom = new Geometry("Floor", quad); // 几何体
        geom.setMaterial(mat); // 设置材质
        geom.rotate(-FastMath.HALF_PI, 0, 0); // 旋转到水平

        root.attachChild(geom); // 添加到场景
    }

    private void initCubes() {
        float scalar = 20;
        float side = 3f;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Geometry geom = new Geometry("Cube", new Box(side, side*2, side));
                geom.setMaterial(getMaterial(new ColorRGBA(1 - x / 8f, y / 8f, 1f, 1f)));
                geom.move((x + 1) * scalar, side*2, -(y + 1) * scalar);
                root.attachChild(geom);
            }
        }
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
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setLocalScale(350); // 天空盒大小
        root.attachChild(sky);
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

        app.getRootNode().attachChild(root);
        app.getRootNode().addLight(ambientLight);
        // app.getRootNode().addLight(point);
        app.getRootNode().addLight(sunLight);
    }

    @Override
    protected void onDisable() {
        SimpleApplication app = (SimpleApplication) getApplication();

        app.getRootNode().detachChild(root);
        app.getRootNode().removeLight(ambientLight);
        app.getRootNode().removeLight(pointLight);
        app.getRootNode().removeLight(sunLight);
    }

    @Override
    protected void cleanup(Application app) {}
}
