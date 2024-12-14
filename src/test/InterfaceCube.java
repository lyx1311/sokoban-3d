import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.Texture;

public class InterfaceCube extends SimpleApplication {
    public static void main(String[] args) {
        InterfaceCube app = new InterfaceCube();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Mesh cube = new com.jme3.scene.shape.Box(1, 1, 1);
        Geometry cubeGeo = new Geometry("Cube", cube);
        rootNode.attachChild(cubeGeo);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture("images/box - interface.png"));
        cubeGeo.setMaterial(mat);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1, -2, -3));
        rootNode.addLight(sun);
        cam.setLocation(new Vector3f(2.9757807f, 4.9841127f, 7.189121f));
        cam.setRotation(new Quaternion(-0.07631536f, 0.92353964f, -0.2741623f, -0.2570712f));
    }
}
