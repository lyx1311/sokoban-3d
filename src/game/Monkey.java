package game;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;

/**
 * 动画
 *
 * @author yanmaoyuan
 *
 */
public abstract class Monkey {
    /**
     * 动画模型
     */
    private static Spatial spatial;

    private static AnimControl animControl;
    private static AnimChannel animChannel;
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
    }

    public static void remove() {
        spatial.removeFromParent();
    }
}