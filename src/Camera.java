/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;

public class Camera {
    public enum Mode {
        PERSPECTIVE, TOP, BOTTOM, LEFT, RIGHT, FRONT, BACK
    };

    protected Mode mode = Mode.PERSPECTIVE;

    protected Math3d.Double3 rayDir = new Math3d.Double3();
    protected Math3d.Double3 intersectionNear = new Math3d.Double3();
    protected Math3d.Double3 intersectionFar = new Math3d.Double3();

    protected Math3d.Double3 orthographicTranslation = new Math3d.Double3();

    private Math3d.Double3 eyePos = new Math3d.Double3();
    private Math3d.Double3 lookAt = new Math3d.Double3(0.0, 0.0, 0.0);
    private double eyeDistance = 30.0; // = zoom
    private Math3d.Double3 rotation = new Math3d.Double3(Math.PI / 4, Math.PI, 0.0);

    private final double minZoom = 2.0;
    private final double maxZoom = 500.0;

    public Camera() {
    }

    public void zoom(double change) {
        eyeDistance += change;
        if (eyeDistance < minZoom)
            eyeDistance = minZoom;
        if (eyeDistance > maxZoom)
            eyeDistance = maxZoom;
    }

    public void pan(double changeXZ, double changeY) {
        if (mode == Mode.PERSPECTIVE) {
            Math3d.Double3 dir = eyePos.clone();
            dir.sub(lookAt);
            dir.normalize();

            Math3d.Double3 upVec = new Math3d.Double3(0.0, 1.0, 0.0);
            Math3d.Double3 right = Math3d.Double3.crossProduct(dir, upVec);
            right.normalize();
            Math3d.Double3 up = Math3d.Double3.crossProduct(dir, right);
            up.normalize();

            right.scalarMul(changeXZ);
            up.scalarMul(changeY);

            lookAt.add(right);
            lookAt.add(up);
        } else {
            orthographicTranslation.x += changeXZ;
            orthographicTranslation.y += changeY;
        }
    }

    public void rotate(double changeXZ, double changeY) {
        rotation.x += changeXZ;
        rotation.x %= Math.PI * 2.0;
        rotation.y += changeY;
        rotation.y %= Math.PI * 2.0;
        if (rotation.x >= (Math.PI / 2.0 - 0.00001))
            rotation.x = (Math.PI / 2.0 - 0.00001);
        if (rotation.x <= -(Math.PI / 2.0 - 0.00001))
            rotation.x = -(Math.PI / 2.0 - 0.00001);
    }

    public void update(GL2 gl, GLU glu) {
        switch (mode) {
            case PERSPECTIVE:
                eyePos.set(0.0, 0.0, -eyeDistance);
                Math3d.Double4x4 rot = new Math3d.Double4x4();
                rot.rotX(rotation.x);
                rot.mulVec(eyePos);
                rot.rotY(rotation.y);
                rot.mulVec(eyePos);
                eyePos.add(lookAt);
                glu.gluLookAt(eyePos.x, eyePos.y, eyePos.z,
                        lookAt.x, lookAt.y, lookAt.z, 0.0, 1.0, 0.0);
                break;
            case TOP:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            case BOTTOM:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
                break;
            case LEFT:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case RIGHT:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
                break;
            case FRONT:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(0.0f, 0.0f, 1.0f, 0.0f);
                break;
            case BACK:
                gl.glScalef(1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance, 1.0f / (float) eyeDistance);
                gl.glTranslatef((float) orthographicTranslation.x, (float) orthographicTranslation.y,
                        (float) orthographicTranslation.z);
                gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
                break;
        }
    }

    public Math3d.Double3 getEyePos() {
        return eyePos;
    }

    public Math3d.Double3 getRotation() {
        Math3d.Double3 rot = eyePos.clone();
        rot.sub(lookAt);
        rot.normalize();
        return rot;
    }

    public double getZoom() {
        return eyeDistance;
    }
}
