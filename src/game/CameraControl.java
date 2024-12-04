package game;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CameraControl extends AbstractControl {
    private Vector3f startPosition, endPosition;
    private Quaternion startRotation, endRotation;
    private float duration, elapsed;
    private boolean isMoving = false, isRotating = false, isFlying = false;
    private Vector3f originLocation;
    private Quaternion originRotation;

    public final boolean isMoving() { return isMoving; }
    public final boolean isRotating() { return isRotating; }
    public final boolean isFlying() { return isFlying; }

    public void moveCamera(Vector3f startPosition, Vector3f endPosition, float duration) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.duration = duration;
        elapsed = 0;
        isMoving = true;
    }

    public void rotateCamera(Quaternion startRotation, Quaternion endRotation, float duration) {
        this.startRotation = startRotation;
        this.endRotation = endRotation;
        this.duration = duration;
        elapsed = 0;
        isRotating = true;
    }

    public void startFlying() {
        originLocation = spatial.getLocalTranslation().clone();
        originRotation = spatial.getLocalRotation().clone();
        isFlying = true;
        spatial.setLocalTranslation(new Vector3f(77.87959f, 122.350235f, 51.420307f));
        spatial.setLocalRotation(new Quaternion(-0.0010890292f, 0.9283413f, -0.37171733f, -0.0027197748f));
    }
    public void stopFlying() {
        spatial.setLocalTranslation(originLocation);
        spatial.setLocalRotation(originRotation);
        isFlying = false;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (isMoving) {
            elapsed += tpf;
            float t = Math.min(elapsed / duration, 1);
            Vector3f currentPosition = startPosition.interpolateLocal(endPosition, t);
            spatial.setLocalTranslation(currentPosition);
            if (currentPosition.equals(endPosition)) isMoving = false;
        }

        if (isRotating) {
            elapsed += tpf;
            float t = Math.min(elapsed / duration, 1);
            Quaternion currentRotation = new Quaternion();
            currentRotation.slerp(startRotation, endRotation, t);
            spatial.setLocalRotation(currentRotation);
            if (currentRotation.equals(endRotation)) isRotating = false;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}