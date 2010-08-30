/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.texture.*;

public class Graphics implements GLEventListener {
    private GuiMainFrame mainFrame;

    int height, width;

    private Texture textureFont;
    private Texture textureStones;

    protected Camera camera;

    protected Object3d obj3dStones;

    private GLU glu;

    // ** intersection-ray ***
    int[] viewport = new int[4];
    double[] modelView = new double[16];
    double[] projection = new double[16];
    double[] posNear = new double[4];
    double[] posFar = new double[4];

    float[] light0Dir = new float[4];

    public Graphics(GuiMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.glu = new GLU();

        obj3dStones = new Object3d();
        obj3dStones.loadFromFile("stones.txt");

        camera = new Camera();
    }

    /**
     * perform only OpenGL-tasks! (maybe called more than once)
     */
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("init GL2 is: " + gl.getClass().getName());

        gl.setSwapInterval(1); // enable VSync

        gl.glClearColor(0.8f, 0.8f, 0.8f, 0.0f);
        gl.glShadeModel(GL2.GL_SMOOTH); // GL_FLAT
        gl.glEnable(GL2.GL_DEPTH_TEST);

        // load textures
        this.textureFont = GraphicsHelper.loadTexture(gl, "font.png");
        this.textureStones = GraphicsHelper.loadTexture(gl, "stones.png");

        // enable alpha blending
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // enable cull-mode
        // gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        // gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);
        // gl.glCullFace(GL2.GL_BACK);
        // gl.glEnable(GL2.GL_CULL_FACE);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // shade model
        gl.glShadeModel(GL2.GL_SMOOTH);

        // material (use glColor3d(..))
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        // lighting
        float[] ambient = { 0.25f, 0.25f, 0.25f, 1.0f };
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ambient, 0);

        float[] light0Ambient = { 0.25f, 0.25f, 0.25f, 1.0f };
        float[] light0Diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
        float[] light0Specular = { 0.2f, 0.2f, 0.2f, 1.0f };
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0Dir, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light0Ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light0Diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light0Specular, 0);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
    }

    double tmp = 0.0;

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // ***** view-matrix *****
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        camera.update(gl, glu);

        // ***** set light direction *****
        // Math3d.Double3 l0dir = camera.getRotation();
        Math3d.Double3 l0dir = new Math3d.Double3(1.0, 0.8, 0.5);
        l0dir.normalize();
        light0Dir[0] = (float) l0dir.x;
        light0Dir[1] = (float) l0dir.y;
        light0Dir[2] = (float) l0dir.z;
        light0Dir[3] = 0.0f; // for directional light w has to be 0*/
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0Dir, 0);

        // ***** update intersection-ray *****
        calculateIntersectionRay(gl);

        if (camera.mode != Camera.Mode.PERSPECTIVE)
            gl.glDisable(GL2.GL_LIGHTING);
        // ***** render current model *****
        gl.glColor3d(1.0, 1.0, 1.0);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        textureStones.enable(gl);
        textureStones.bind(gl);
        this.mainFrame.getGuiFSMReference().currentModel.render(gl);

        // ***** update position of current stones *****
        mainFrame.getGuiFSMReference().updatePlacementPos();

        // ***** draw current stones *****
        GuiFSM guiFsm = mainFrame.getGuiFSMReference();
        GraphicsHelper.setColor(gl, guiFsm.currentStonesColor);
        gl.glPushMatrix();
        Model.Stone stone;
        for (Iterator<Model.Stone> it = guiFsm.currentStones.iterator(); it.hasNext();) {
            stone = (Model.Stone) it.next();
            obj3dStones.renderBySubId(gl, stone.type,
                    stone.pos.x, 0.4 * stone.pos.y, stone.pos.z, 0, stone.rotation * 90.0, 0);
        }
        gl.glPopMatrix();
        if (camera.mode != Camera.Mode.PERSPECTIVE)
            gl.glEnable(GL2.GL_LIGHTING);

        // ***** render grid *****
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glLineWidth(0.5f);
        gl.glPushMatrix();
        if (guiFsm.holdHeight) {
            gl.glColor3d(0.5, 0.3, 1.0);
            gl.glTranslated(0.0, guiFsm.currentStonesPos.y * 0.4, 0.0);
        } else {
            gl.glColor3d(0.75, 0.75, 0.75);
            gl.glTranslated(0.0, guiFsm.getGridHeight() * 0.4, 0.0);
        }
        GraphicsHelper.grid3d(gl, 100.0, 1.0);
        gl.glPopMatrix();
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glEnable(GL2.GL_LIGHTING);

        // ***** render move-tool *****
        if (guiFsm.renderMoveTool) {
            gl.glDisable(GL2.GL_DEPTH_TEST);
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glLineWidth(3.0f);
            gl.glPushMatrix();
            gl.glTranslated(guiFsm.moveToolPos.x, guiFsm.moveToolPos.y,
                    guiFsm.moveToolPos.z);
            double length = 0.1 * camera.getZoom();
            gl.glBegin(GL2.GL_LINES);
            // x
            gl.glColor3d(0.2, 1.0, 0.2);
            gl.glVertex3d(0.0, 0.0, 0.0);
            gl.glVertex3d(length, 0.0, 0.0);
            gl.glVertex3d(length * 0.9, 0.0, length * 0.1);
            gl.glVertex3d(length, 0.0, 0.0);
            gl.glVertex3d(length * 0.9, 0.0, -length * 0.1);
            gl.glVertex3d(length, 0.0, 0.0);
            gl.glVertex3d(length * 0.9, length * 0.1, 0.0);
            gl.glVertex3d(length, 0.0, 0.0);
            gl.glVertex3d(length * 0.9, -length * 0.1, 0.0);
            gl.glVertex3d(length, 0.0, 0.0);
            // y
            gl.glColor3d(1.0, 0.2, 0.2);
            gl.glVertex3d(0.0, 0.0, 0.0);
            gl.glVertex3d(0.0, length, 0.0);

            gl.glVertex3d(length * 0.1, length * 0.9, 0.0);
            gl.glVertex3d(0.0, length, 0.0);
            gl.glVertex3d(-length * 0.1, length * 0.9, 0.0);
            gl.glVertex3d(0.0, length, 0.0);

            gl.glVertex3d(0.0, length * 0.9, length * 0.1);
            gl.glVertex3d(0.0, length, 0.0);
            gl.glVertex3d(0.0, length * 0.9, -length * 0.1);
            gl.glVertex3d(0.0, length, 0.0);

            // z
            gl.glColor3d(0.2, 0.2, 1.0);
            gl.glVertex3d(0.0, 0.0, 0.0);
            gl.glVertex3d(0.0, 0.0, length);
            gl.glVertex3d(length * 0.1, 0.0, length * 0.9);
            gl.glVertex3d(0, 0.0, length);
            gl.glVertex3d(-length * 0.1, 0.0, length * 0.9);
            gl.glVertex3d(0, 0.0, length);
            gl.glVertex3d(0.0, length * 0.1, length * 0.9);
            gl.glVertex3d(0, 0.0, length);
            gl.glVertex3d(0.0, -length * 0.1, length * 0.9);
            gl.glVertex3d(0, 0.0, length);

            gl.glEnd();
            gl.glPopMatrix();
            gl.glEnable(GL2.GL_DEPTH_TEST);
            gl.glEnable(GL2.GL_LIGHTING);
        }

        // gl.glFlush();
        drawable.swapBuffers();
    }

    private void calculateIntersectionRay(GL2 gl) {
        int mouseX = this.mainFrame.getGraphicsWindowsMousePosX();
        int mouseY = this.mainFrame.getGraphicsWindowsMousePosY();
        if (mouseX == -1 || mouseY == -1)
            return;
        // ***** calc picking-ray *****
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);
        int windowX = mouseX;
        int windowY = viewport[3] - mouseY;
        glu.gluUnProject(windowX, windowY, 0.0, modelView, 0, projection, 0, viewport, 0, posNear, 0);
        glu.gluUnProject(windowX, windowY, 1.0, modelView, 0, projection, 0, viewport, 0, posFar, 0);

        // System.out.println("near: "+posNear[0]+" "+posNear[1]+" "+posNear[2]);
        // System.out.println("far: "+posFar [0]+" "+posFar [1]+" "+posFar [2]);
        camera.intersectionNear.set(posNear[0], posNear[1], posNear[2]);
        camera.intersectionFar.set(posFar[0], posFar[1], posFar[2]);

        camera.rayDir.x = posFar[0] - posNear[0];
        camera.rayDir.y = posFar[1] - posNear[1];
        camera.rayDir.z = posFar[2] - posNear[2];
        camera.rayDir.normalize();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;

        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0)
            height = 1;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        float h = (float) width / (float) height;

        if (camera.mode == Camera.Mode.PERSPECTIVE)
            glu.gluPerspective(45.0f, h, 1.0, 500.0);
        else if (h >= 1)
            gl.glOrtho(-1.0, 1.0, -1.0 / h, 1.0 / h, -1000.0, 1000.0);
        else
            gl.glOrtho(-1.0 * h, 1.0 * h, -1.0, 1.0, -1000.0, 1000.0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        if (this.textureFont != null) {
            this.textureFont.destroy(gl);
        }

        if (this.textureStones != null) {
            this.textureStones.destroy(gl);
        }

    }

}
