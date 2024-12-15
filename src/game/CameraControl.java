package game;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CameraControl extends AbstractControl {
    private static final float EPS = 1e-4f;
    private static final float FLY_CAM_MOVE_SPEED = 80f;

    private Vector3f startPosition, endPosition, originLocation, moveDirection = null;
    private Quaternion startRotation, endRotation, originRotation;
    private float duration, elapsed;
    private boolean isMoving = false, isRotating = false, isFlying = false;

    public final boolean isMoving() { return isMoving; }
    public final boolean isRotating() { return isRotating; }
    public final boolean isFlying() { return isFlying; }
    public final boolean isMovingFlyCam() { return moveDirection != null; }

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

    public void startFly() {
        originLocation = spatial.getLocalTranslation().clone();
        originRotation = spatial.getLocalRotation().clone();
        isFlying = true;
        spatial.setLocalTranslation(new Vector3f(40f, 40f, 40f));
        spatial.setLocalRotation(new Quaternion(0f, 0.93f, -0.37f, 0f));
    }
    public void stopFly() {
        spatial.setLocalTranslation(originLocation);
        spatial.setLocalRotation(originRotation);
        isFlying = false;
    }
    public void startMoveFlyCam(Vector3f direction) { moveDirection = direction; }
    public void stopMoveFlyCam() { moveDirection = null; }

    private static boolean isEqual(Vector3f a, Vector3f b) { return a.cross(b).lengthSquared() < EPS; }
    private static boolean isEqual(Quaternion a, Quaternion b) { return a.dot(b) > 1 - EPS; }

    @Override
    protected void controlUpdate(float tpf) {
        if (isMoving) {
            elapsed += tpf;
            float t = Math.min(elapsed / duration, 1);
            Vector3f currentPosition = startPosition.interpolateLocal(endPosition, t);
            spatial.setLocalTranslation(currentPosition);
            if (isEqual(currentPosition, endPosition)) {
                spatial.setLocalTranslation(endPosition);
                isMoving = false;
            }
        }

        if (isRotating) {
            elapsed += tpf;
            float t = Math.min(elapsed / duration, 1);
            Quaternion currentRotation = new Quaternion();
            currentRotation.slerp(startRotation, endRotation, t);
            spatial.setLocalRotation(currentRotation);
            if (isEqual(currentRotation, endRotation)) {
                spatial.setLocalRotation(endRotation);
                System.out.println(" * Camera direction: " + spatial.getLocalTranslation());
                isRotating = false;
            }
        }

        if (moveDirection != null && spatial.getLocalTranslation().add(moveDirection.mult(FLY_CAM_MOVE_SPEED * tpf)).y
                < CubeState.MAX_HEIGHT * 2) {
            spatial.move(moveDirection.mult(FLY_CAM_MOVE_SPEED * tpf));
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}