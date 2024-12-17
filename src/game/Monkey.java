package game;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;

public abstract class Monkey {
    private static Spatial spatial;
    private static AnimControl animControl;
    private static AnimChannel animChannel;
    private static boolean isAdded = false;

    public static void add(Application app, float x, float y, float z, Quaternion rotation) {
        // 加载Jaime模型
        spatial = app.getAssetManager().loadModel("Models/Jaime/Jaime.j3o");
        spatial.setLocalScale(15f);;
        spatial.setLocalTranslation(x, y, z);
        spatial.setLocalRotation(rotation);

        // 动画控制器
        animControl = spatial.getControl(AnimControl.class);
        animChannel = animControl.createChannel();
        animChannel.setAnim("Idle");

        if (app instanceof SimpleApplication) {
            ((SimpleApplication) app).getRootNode().attachChild(spatial);
        } else {
            throw new IllegalArgumentException("Application is not an instance of SimpleApplication");
        }

        isAdded = true;
    }

    public static void remove() {
        spatial.removeFromParent();
        isAdded = false;
    }

    public static boolean isAdded() { return isAdded; }
}