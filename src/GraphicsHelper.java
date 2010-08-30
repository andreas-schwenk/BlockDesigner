/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.io.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

public class GraphicsHelper {
    private static final double DIVISIONS = 24.0;

    public static Texture loadTexture(GL2 gl, String filename) {
        Texture texture = null;
        try {
            // texture = TextureIO.newTexture(new File(filename), true);
            InputStream stream = GraphicsHelper.class.getResourceAsStream("/" + filename);
            texture = TextureIO.newTexture(stream, true, TextureIO.PNG);

            texture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            texture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            texture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            texture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
        } catch (Exception e) {
            System.out.println("loadTexture() failed (" + filename + ")");
        }
        return texture;
    }

    public static void text(GL2 gl, double x, double y, char[] text) {
        double letterSizeX = 8.0, letterSizeY = 13.0;

        gl.glColor3d(0.75, 0.75, 0.75);

        double sizeX = letterSizeX / 256.0;
        double sizeY = letterSizeY / 256.0;

        double translX = 0.0, translY = 0.0;
        double texCoordX, texCoordY;

        int i = 0;
        while (i < text.length) {
            if (text[i] >= 32 && text[i] < 128) {
                texCoordX = sizeX * (double) ((text[i] - 33) % 16);
                texCoordY = sizeY * (double) ((text[i] - 33) / 16);

                gl.glBegin(GL2.GL_QUADS);
                gl.glTexCoord2d(texCoordX, 1.0 - texCoordY);
                gl.glVertex3d(translX + x, translY + y, 0.0);

                gl.glTexCoord2d(texCoordX + sizeX, 1.0 - texCoordY);
                gl.glVertex3d(translX + x + letterSizeX, translY + y, 0.0);

                gl.glTexCoord2d(texCoordX + sizeX, 1.0 - (texCoordY + sizeY));
                gl.glVertex3d(translX + x + letterSizeX, translY + y + letterSizeY, 0.0);

                gl.glTexCoord2d(texCoordX, 1.0 - (texCoordY + sizeY));
                gl.glVertex3d(translX + x, translY + y + letterSizeY, 0.0);
                gl.glEnd();

                translX += letterSizeX;
            } else if (text[i] == '\n') {
                translX = 0.0;
                translY += letterSizeY;
            }
            i++;
        }
    }

    public static void arc(GL2 gl, double centerX, double centerY, double width, double height, double startAngle,
            double angle) {
        double start = -startAngle / 360.0 * DIVISIONS;
        double end = (-startAngle - angle) / 360.0 * DIVISIONS;

        width /= 2.0;
        height /= 2.0;

        if (start > end) {
            double tmp = start;
            start = end;
            end = tmp;
        }

        gl.glBegin(GL2.GL_LINE_STRIP);
        for (double i = start; i <= end; i += (end - start) / DIVISIONS * (360.0 / angle)) {
            gl.glVertex3d(
                    centerX + width * Math.cos(i / DIVISIONS * 2.0f * Math.PI),
                    centerY + height * Math.sin(i / DIVISIONS * 2.0f * Math.PI),
                    0.0);
        }
        gl.glEnd();
    }

    public static void ellipse(GL2 gl, double width, double height) {
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (double i = 0.0f; i <= DIVISIONS; i += 1.0f) {
            gl.glVertex3d(
                    width * Math.cos(i / DIVISIONS * 2.0f * Math.PI),
                    height * Math.sin(i / DIVISIONS * 2.0f * Math.PI),
                    0.0);
        }
        gl.glEnd();
    }

    public static void circle(GL2 gl, double radius) {
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (double i = 0.0f; i <= DIVISIONS; i += 1.0f) {
            gl.glVertex3d(
                    radius * Math.cos(i / DIVISIONS * 2.0f * Math.PI),
                    radius * Math.sin(i / DIVISIONS * 2.0f * Math.PI),
                    0.0);
        }
        gl.glEnd();
    }

    public static void circle(GL2 gl, double centerX, double centerY, double radius) {
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (double i = 0.0f; i <= DIVISIONS; i += 1.0f) {
            gl.glVertex3d(
                    centerX + radius * Math.cos(i / DIVISIONS * 2.0f * Math.PI),
                    centerY + radius * Math.sin(i / DIVISIONS * 2.0f * Math.PI),
                    0.0);
        }
        gl.glEnd();
    }

    public static void grid2d(GL2 gl) {
        gl.glBegin(GL2.GL_LINES);
        double i;
        for (i = 0.0; i <= 500.0; i += 10.0) {
            gl.glVertex3d(i, 0.0, 0.0);
            gl.glVertex3d(i, 300.0, 0.0);
        }
        for (i = 0.0; i <= 300.0; i += 10.0) {
            gl.glVertex3d(0.0, i, 0.0);
            gl.glVertex3d(500.0, i, 0.0);
        }
        gl.glEnd();
    }

    public static void grid3d(GL2 gl, double dia, double step) {
        gl.glBegin(GL2.GL_LINES);
        double i;
        for (i = -dia / 2.0; i < dia / 2.0; i += step) {
            gl.glVertex3d(i, 0.0, -dia / 2.0);
            gl.glVertex3d(i, 0.0, dia / 2.0);
        }
        for (i = -dia / 2.0; i < dia / 2.0; i += step) {
            gl.glVertex3d(-dia / 2.0, 0.0, i);
            gl.glVertex3d(dia / 2.0, 0.0, i);
        }
        gl.glEnd();
    }

    public static void box3d(GL2 gl) {
        gl.glBegin(GL2.GL_TRIANGLES);
        // TOP
        gl.glVertex3d(-1, 1, -1);
        gl.glVertex3d(-1, 1, 1);
        gl.glVertex3d(1, 1, -1);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(1, 1, -1);
        gl.glVertex3d(-1, 1, 1);
        // BOTTOM
        gl.glVertex3d(1, -1, -1);
        gl.glVertex3d(-1, -1, 1);
        gl.glVertex3d(-1, -1, -1);
        gl.glVertex3d(-1, -1, 1);
        gl.glVertex3d(1, -1, -1);
        gl.glVertex3d(1, -1, 1);
        // LEFT
        gl.glVertex3d(-1, 1, -1);
        gl.glVertex3d(-1, -1, -1);
        gl.glVertex3d(-1, 1, 1);
        gl.glVertex3d(-1, -1, 1);
        gl.glVertex3d(-1, 1, 1);
        gl.glVertex3d(-1, -1, -1);
        // RIGHT
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(1, -1, -1);
        gl.glVertex3d(1, 1, -1);
        gl.glVertex3d(1, -1, -1);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(1, -1, 1);
        // FRONT
        gl.glVertex3d(-1, 1, 1);
        gl.glVertex3d(-1, -1, 1);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(1, -1, 1);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(-1, -1, 1);
        // BACK
        gl.glVertex3d(1, 1, -1);
        gl.glVertex3d(-1, -1, -1);
        gl.glVertex3d(-1, 1, -1);
        gl.glVertex3d(-1, -1, -1);
        gl.glVertex3d(1, 1, -1);
        gl.glVertex3d(1, -1, -1);
        gl.glEnd();
    }

    public static void renderMenu(GL2 gl, int x, int y, String headline,
            String[] captions, int width, int indexSelected) {
        double height;
        int hasHeadline;

        if (headline.equals(""))
            hasHeadline = 0;
        else
            hasHeadline = 1;

        height = (double) (captions.length + hasHeadline) * 20.0;

        // background
        gl.glBlendFunc(GL2.GL_SRC_COLOR, GL2.GL_ONE_MINUS_SRC_COLOR);
        for (int i = 0; i < 2; i++) // render twice to achieve a higher opacity
        {
            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glColor3d(0.85, 0.85, 0.85);
            gl.glVertex3d((double) x, (double) y, 0.0);
            gl.glVertex3d((double) x + width, (double) y, 0.0);
            gl.glVertex3d((double) x, (double) y + height, 0.0);
            gl.glColor3d(0.77, 0.77, 0.77);
            gl.glVertex3d((double) x + width, (double) y + height, 0.0);
            gl.glVertex3d((double) x, (double) y + height, 0.0);
            gl.glVertex3d((double) x + width, (double) y, 0.0);
            // headline background
            if (hasHeadline == 1) {
                gl.glColor3d(0.6, 0.6, 0.6);
                gl.glVertex3d((double) x, (double) y, 0.0);
                gl.glVertex3d((double) x + width, (double) y, 0.0);
                gl.glVertex3d((double) x, (double) y + 20, 0.0);

                gl.glVertex3d((double) x + width, (double) y + 20, 0.0);
                gl.glVertex3d((double) x, (double) y + 20, 0.0);
                gl.glVertex3d((double) x + width, (double) y, 0.0);
            }
            gl.glEnd();
        }
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        if (indexSelected != -1) {
            gl.glColor3d(1.0, 0.5, 0.0);
            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glVertex3d((double) x, (double) y + (indexSelected + hasHeadline) * 20, 0.0);
            gl.glVertex3d((double) x + width, (double) y + (indexSelected + hasHeadline) * 20, 0.0);
            gl.glVertex3d((double) x, (double) y + (indexSelected + hasHeadline + 1) * 20, 0.0);
            gl.glVertex3d((double) x + width, (double) y + (indexSelected + hasHeadline + 1) * 20, 0.0);
            gl.glVertex3d((double) x, (double) y + (indexSelected + hasHeadline + 1) * 20, 0.0);
            gl.glVertex3d((double) x + width, (double) y + (indexSelected + hasHeadline) * 20, 0.0);
            gl.glEnd();
        }

        // border
        gl.glColor3d(0.0, 0.0, 0.0);
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3d((double) x, (double) y, 0.0);
        gl.glVertex3d((double) x + width, (double) y, 0.0);
        gl.glVertex3d((double) x + width, (double) y + height, 0.0);
        gl.glVertex3d((double) x, (double) y + height, 0.0);
        gl.glVertex3d((double) x, (double) y, 0.0);
        gl.glEnd();

        // arrows
        gl.glBegin(GL2.GL_TRIANGLES);
        for (int i = 0; i < captions.length; i++) {
            if (captions[i].charAt(0) != '.')
                continue;
            gl.glVertex3d(x + width - 12, y + (i + hasHeadline) * 20 + 6, 0.0);
            gl.glVertex3d(x + width - 12 + 7, y + (i + hasHeadline) * 20 + 4 + 6, 0.0);
            gl.glVertex3d(x + width - 12, y + (i + hasHeadline) * 20 + 7 + 6, 0.0);
        }
        gl.glEnd();
        // text
        gl.glEnable(GL2.GL_TEXTURE_2D);
        // headline
        GraphicsHelper.text(gl, x + 5, y + 2, headline.toCharArray());
        // captions
        for (int i = 0; i < captions.length; i++) {
            if (captions[i].charAt(0) != '.')
                GraphicsHelper.text(gl, x + 5, y + (i + hasHeadline) * 20 + 3, captions[i].toCharArray());
            else
                GraphicsHelper.text(gl, x + 5, y + (i + hasHeadline) * 20 + 3,
                        captions[i].substring(1, captions[i].length()).toCharArray());
        }

        gl.glDisable(GL2.GL_TEXTURE_2D);
    }

    public static void renderCross(GL2 gl) {
        gl.glColor3d(0.0, 0.0, 0.0);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3d(-0.5, -0.5, 0);
        gl.glVertex3d(0.5, 0.5, 0);
        gl.glVertex3d(0.5, -0.5, 0);
        gl.glVertex3d(-0.5, 0.5, 0);
        gl.glEnd();
    }

    public static void setColor(GL2 gl, int color) {
        switch (color) {
            case 0:
                gl.glColor3d(1.0, 1.0, 1.0);
                break; // white
            case 1:
                gl.glColor3d(1.0, 1.0, 0.0);
                break; // yellow
            case 2:
                gl.glColor3d(0.7, 0.3, 0.3);
                break; // red
            case 3:
                gl.glColor3d(0.0, 0.0, 1.0);
                break; // blue
            case 4:
                gl.glColor3d(0.3, 0.3, 0.3);
                break; // black
            case 5:
                gl.glColor3d(0.1, 0.8, 0.1);
                break; // green
            case 6:
                gl.glColor3d(0.5, 0.3, 0.0);
                break; // brown
            case 7:
                gl.glColor3d(0.6, 0.6, 0.6);
                break; // gray
            default:
                System.out.println("Error: setColor(..): wrong color index");
        }
    }

}
