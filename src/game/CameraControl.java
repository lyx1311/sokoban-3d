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
    private boolean isMoving, isRotating;

    public final boolean isMoving() { return isMoving; }
    public final boolean isRotating() { return isRotating; }

    public void moveCamera(Vector3f startPosition, Vector3f endPosition, float duration) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.duration = duration;
        this.elapsed = 0;
        this.isMoving = true;
    }

    public void rotateCamera(Quaternion startRotation, Quaternion endRotation, float duration) {
        this.startRotation = startRotation;
        this.endRotation = endRotation;
        this.duration = duration;
        this.elapsed = 0;
        this.isRotating = true;
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