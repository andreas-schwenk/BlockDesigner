/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.io.*;
import java.util.*;

public class GuiFSM {
    private GuiMainFrame mainFrame;
    private Graphics graphics;

    private int gridHeight;

    public enum State {
        SELECT, INSERT, INSERT_DRAGGING
    };

    private State state = State.INSERT;

    protected int currentInsertID = 0;

    protected boolean additiveSelection = false;
    protected boolean holdHeight = false;

    protected boolean renderMoveTool = false;
    protected Math3d.Double3 moveToolPos = new Math3d.Double3();

    protected Model currentModel;

    protected LinkedList<Model.Stone> currentStones = null;
    protected int currentStonesType = 432;
    protected int currentStonesColor = 0;
    protected Math3d.Int3 currentStonesPos = new Math3d.Int3();
    protected int currentStonesRotation = 0;
    protected int currentStonesStackHeight = 1;
    protected Math3d.Int3 currentStonesDragPos = new Math3d.Int3();
    protected boolean currentStonesRectFilled = false;
    protected boolean currentStonesEdgeWall = false;

    protected boolean viewRotating = false;
    protected Math3d.Int2 viewRotatingStartPos = new Math3d.Int2();
    protected boolean viewPanning = false;
    protected Math3d.Int2 viewPanningStartPos = new Math3d.Int2();

    GuiFSM(GuiMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.graphics = mainFrame.getGraphicsReference();

        updateCurrentStones();

        currentModel = mainFrame.addModel();
    }

    public void updatePlacementPos() {
        Math3d.Int3 pos;
        if (graphics.camera.mode == Camera.Mode.PERSPECTIVE) {
            pos = currentModel.getPlacementPos(
                    graphics.camera.getEyePos(), graphics.camera.rayDir, gridHeight,
                    holdHeight || state == State.INSERT_DRAGGING);
        } else // orthographic
        {
            pos = currentModel.getPlacementPos(
                    graphics.camera.intersectionNear, graphics.camera.rayDir, gridHeight,
                    holdHeight || state == State.INSERT_DRAGGING);
        }
        if (state == GuiFSM.State.INSERT_DRAGGING)
            currentStonesDragPos = pos;
        else
            currentStonesPos = pos;
        updateCurrentStones();
    }

    private void insertStoneLineOfRect(Math3d.Int3 pos, int dest,
            int stoneLength, int stoneWidth,
            boolean xAxis, boolean insert, boolean rotated,
            boolean nextRight, boolean nextDown) {
        int value;
        if (xAxis)
            value = pos.x;
        else
            value = pos.z;
        int diff;
        boolean growing = value <= dest;
        while ((diff = Math.abs(value - dest)) >= stoneLength) {
            if (value > dest)
                value -= stoneLength;
            else
                value += stoneLength;
            if (xAxis)
                pos.x = value;
            else
                pos.z = value;
            if (insert) {
                Model.Stone stone = new Model.Stone();
                stone.type = currentStonesType;
                stone.color = currentStonesColor;
                stone.rotation = currentStonesRotation;
                if (rotated)
                    stone.rotation++;
                stone.pos.set(pos);
                currentStones.add(stone);
            }
        }
        int offset = 0;
        if (diff >= stoneWidth)
            offset = stoneLength - 1;
        else
            offset = stoneWidth - 1;

        if (!xAxis && !growing && !nextRight) {
            pos.x += offset;
            pos.z -= offset;
        } else if (!xAxis && !growing && nextRight) {
            pos.x -= offset;
            pos.z -= offset;
        } else if (!xAxis && growing && !nextRight) {
            pos.x += offset;
            pos.z += offset;
        } else if (!xAxis && growing && nextRight) {
            pos.x -= offset;
            pos.z += offset;
        }

        else if (xAxis && !growing && !nextDown) {
            pos.x -= offset;
            pos.z += offset;
        } else if (xAxis && !growing && nextDown) {
            pos.x -= offset;
            pos.z -= offset;
        } else if (xAxis && growing && !nextDown) {
            pos.x += offset;
            pos.z += offset;
        } else if (xAxis && growing && nextDown) {
            pos.x += offset;
            pos.z -= offset;
        }
    }

    public final void updateCurrentStones() {
        currentStones = new LinkedList<Model.Stone>();

        Math3d.Int3 stoneSize = new Math3d.Int3(
                Model.Stone.getSizeX(currentStonesType),
                Model.Stone.getSizeY(currentStonesType),
                Model.Stone.getSizeZ(currentStonesType));

        if (currentStonesRotation % 2 != 0 && currentStonesRectFilled) {
            // swap
            int tmp = stoneSize.x;
            stoneSize.x = stoneSize.z;
            stoneSize.z = tmp;
        }

        // int x=0, y=0, z=0;
        Math3d.Int3 pos = new Math3d.Int3();

        if (state == State.INSERT) {
            Model.Stone stone;
            pos.x = currentStonesPos.x;
            pos.z = currentStonesPos.z;
            for (pos.y = currentStonesPos.y; pos.y < stoneSize.y * currentStonesStackHeight
                    + currentStonesPos.y; pos.y += stoneSize.y) {
                stone = new Model.Stone();
                stone.type = currentStonesType;
                stone.color = currentStonesColor;
                stone.rotation = currentStonesRotation;
                stone.pos.set(pos);
                currentStones.add(stone);
            }
        } else if (state == State.INSERT_DRAGGING) {
            Model.Stone stone = new Model.Stone();
            stone.type = currentStonesType;
            stone.color = currentStonesColor;
            stone.rotation = currentStonesRotation;
            int row = -1;
            for (pos.y = currentStonesPos.y; pos.y < stoneSize.y * currentStonesStackHeight
                    + currentStonesPos.y; pos.y += stoneSize.y) {
                row++;
                if (currentStonesRectFilled) {
                    pos.x = currentStonesPos.x;
                    while (true) {
                        pos.z = currentStonesPos.z;
                        while (true) {
                            stone = stone.clone();
                            stone.pos.set(pos);
                            currentStones.add(stone);
                            if (Math.abs(pos.z - currentStonesDragPos.z) >= stoneSize.z) {
                                if (pos.z > currentStonesDragPos.z)
                                    pos.z -= stoneSize.z;
                                else
                                    pos.z += stoneSize.z;
                            } else
                                break;
                        }
                        if (Math.abs(pos.x - currentStonesDragPos.x) >= stoneSize.x) {
                            if (pos.x > currentStonesDragPos.x)
                                pos.x -= stoneSize.x;
                            else
                                pos.x += stoneSize.x;
                        } else
                            break;
                    }
                } else // currentStonesRectFilled = false
                {
                    boolean right = currentStonesDragPos.x >= currentStonesPos.x;
                    boolean down = currentStonesDragPos.z >= currentStonesPos.z;
                    // int diff;

                    boolean topWall = false;
                    boolean rightWall = false;
                    boolean bottomWall = false;
                    boolean leftWall = false;

                    if (currentStonesEdgeWall) {
                        if (right && down) {
                            topWall = true;
                            rightWall = true;
                        } else if (down && !right) {
                            leftWall = true;
                            bottomWall = true;
                        } else if (!right && !down) {
                            topWall = true;
                            rightWall = true;
                        } else if (!down && right) {
                            leftWall = true;
                            bottomWall = true;
                        }
                    } else
                        topWall = rightWall = bottomWall = leftWall = true;

                    pos.x = currentStonesPos.x;
                    pos.z = currentStonesPos.z;
                    boolean DC = false;

                    if (currentStonesRotation % 2 == 0) {
                        boolean easyWall = Math.abs(currentStonesDragPos.z - currentStonesPos.z) == 0;
                        if (row % 2 == 0) {
                            if (topWall || !(currentStonesEdgeWall && leftWall || currentStonesEdgeWall && rightWall)) {
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation;
                                stone.pos.set(pos);
                                currentStones.add(stone);
                            }
                            if (easyWall)
                                insertStoneLineOfRect(pos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        topWall, false, DC, down);
                            else {
                                insertStoneLineOfRect(pos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        topWall, false, DC, down);
                                insertStoneLineOfRect(pos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        rightWall, true, !right, DC);
                                insertStoneLineOfRect(pos, currentStonesPos.x, stoneSize.x, stoneSize.z, true,
                                        bottomWall, false, DC, !down);
                                insertStoneLineOfRect(pos, currentStonesPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, true, right, DC);
                            }
                        } else {
                            Math3d.Int3 tmpPos = pos.clone();
                            if (right)
                                tmpPos.x--;
                            else
                                tmpPos.x++;
                            if (down)
                                tmpPos.z++;
                            else
                                tmpPos.z--;
                            if (!easyWall && !currentStonesEdgeWall) {
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation + 1;
                                stone.pos.set(tmpPos);
                                currentStones.add(stone);
                            }
                            if (easyWall) {
                                tmpPos = pos.clone();
                                if (right)
                                    tmpPos.x += 2;
                                else
                                    tmpPos.x -= 2;
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation;
                                stone.pos.set(tmpPos);
                                currentStones.add(stone);
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        topWall, false, DC, down);
                            } else {
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, true, right, DC);
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        bottomWall, false, DC, !down);
                                insertStoneLineOfRect(tmpPos, currentStonesPos.z, stoneSize.x, stoneSize.z, false,
                                        rightWall, true, !right, DC);
                                insertStoneLineOfRect(tmpPos, currentStonesPos.x, stoneSize.x, stoneSize.z, true,
                                        topWall, false, DC, down);
                            }
                        }
                    } else {
                        boolean easyWall = Math.abs(currentStonesDragPos.x - currentStonesPos.x) == 0;
                        if (row % 2 == 0) {
                            if (leftWall
                                    || !(currentStonesEdgeWall && topWall || currentStonesEdgeWall && bottomWall)) {
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation;
                                stone.pos.set(pos);
                                currentStones.add(stone);
                            }
                            if (easyWall)
                                insertStoneLineOfRect(pos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, false, right, DC);
                            else {
                                insertStoneLineOfRect(pos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, false, right, DC);
                                insertStoneLineOfRect(pos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        bottomWall, true, DC, !down);
                                insertStoneLineOfRect(pos, currentStonesPos.z, stoneSize.x, stoneSize.z, false,
                                        rightWall, false, !right, DC);
                                insertStoneLineOfRect(pos, currentStonesPos.x, stoneSize.x, stoneSize.z, true, topWall,
                                        true, DC, down);
                            }
                        } else {
                            Math3d.Int3 tmpPos = pos.clone();
                            if (right)
                                tmpPos.x++;
                            else
                                tmpPos.x--;
                            if (down)
                                tmpPos.z--;
                            else
                                tmpPos.z++;
                            if (!easyWall && !currentStonesEdgeWall) {
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation + 1;
                                stone.pos.set(tmpPos);
                                currentStones.add(stone);
                            }
                            if (easyWall) {
                                tmpPos = pos.clone();
                                if (down)
                                    tmpPos.z += 2;
                                else
                                    tmpPos.z -= 2;
                                stone = new Model.Stone();
                                stone.type = currentStonesType;
                                stone.color = currentStonesColor;
                                stone.rotation = currentStonesRotation;
                                stone.pos.set(tmpPos);
                                currentStones.add(stone);
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, false, right, DC);
                            } else {
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.x, stoneSize.x, stoneSize.z, true,
                                        topWall, true, DC, down);
                                insertStoneLineOfRect(tmpPos, currentStonesDragPos.z, stoneSize.x, stoneSize.z, false,
                                        rightWall, false, !right, DC);
                                insertStoneLineOfRect(tmpPos, currentStonesPos.x, stoneSize.x, stoneSize.z, true,
                                        bottomWall, true, DC, !down);
                                insertStoneLineOfRect(tmpPos, currentStonesPos.z, stoneSize.x, stoneSize.z, false,
                                        leftWall, false, right, DC);
                            }
                        }
                    }

                }
            }
        }
    }

    public void signalDuplicateSelection(Math3d.Int3 transformation) {
        if (this.state == State.SELECT) {
            currentModel.duplicateSelectedStones(transformation, currentInsertID++);
            this.mainFrame.forceRepaint();
        }
    }

    /**
     *
     * @param grpName
     * @return groupID
     */
    public int signalMakeGroupSelection(String grpName) {
        return currentModel.makeGroupFromSelection(grpName);
    }

    public void signalDeleteGroupSelection(int id) {
        currentModel.deleteGroupSelection(id);
    }

    public LinkedList<Model.SelectionGroup> getSelectionGroups() {
        return currentModel.getSelectionGroups();
    }

    public void signalSelectByGroupID(int id) {
        currentModel.selectByGroupID(id);
        this.mainFrame.forceRepaint();
    }

    public void signalKeyArrowLeft() {
        graphics.camera.pan(5, 0);
        this.mainFrame.forceRepaint();
    }

    public void signalKeyArrowRight() {
        graphics.camera.pan(-5, 0);
        this.mainFrame.forceRepaint();
    }

    public void signalKeyArrowUp() {
        graphics.camera.pan(0, -5);
        this.mainFrame.forceRepaint();
    }

    public void signalKeyArrowDown() {
        graphics.camera.pan(0, 5);
        this.mainFrame.forceRepaint();
    }

    public void signalKeyPageUp() {
        this.gridHeight++;
        this.mainFrame.forceRepaint();
    }

    public void signalKeyPageDown() {
        this.gridHeight--;
        this.mainFrame.forceRepaint();
    }

    public void signalSetCurrentStonesType(int type) {
        currentStonesType = type;
    }

    public void signalKeyNumber(int no) {
        if (state == State.INSERT) {
            switch (no) {
                case 0:
                    signalSetCurrentStonesType(1032);
                    break;
                case 1:
                    signalSetCurrentStonesType(131);
                    break;
                case 2:
                    signalSetCurrentStonesType(231);
                    break;
                case 3:
                    signalSetCurrentStonesType(331);
                    break;
                case 4:
                    signalSetCurrentStonesType(232);
                    break;
                case 5:
                    signalSetCurrentStonesType(431);
                    break;
                case 6:
                    signalSetCurrentStonesType(332);
                    break;
                case 7:
                    signalSetCurrentStonesType(632);
                    break;
                case 8:
                    signalSetCurrentStonesType(432);
                    break;
                case 9:
                    signalSetCurrentStonesType(832);
                    break;
                default:
            }
            this.mainFrame.forceRepaint();
        }
    }

    public void signalRotateCurrentStone() {
        if (currentStonesRotation < 3)
            currentStonesRotation++;
        else
            currentStonesRotation = 0;
        updateCurrentStones();
    }

    public void signalKeyLetter(char letter) {
        if (state == State.INSERT) {
            switch (letter) {
                case 'R':
                    signalRotateCurrentStone();
                    break;
            }
            this.mainFrame.forceRepaint();
        }
    }

    public void signalMouseMoved() {
        if (viewRotating) {
            int mouseX = mainFrame.getGraphicsWindowsMousePosX();
            int mouseY = mainFrame.getGraphicsWindowsMousePosY();
            if (mouseX == -1 || mouseY == -1)
                return;
            double x = mouseX - viewRotatingStartPos.x;
            double y = mouseY - viewRotatingStartPos.y;
            x /= 150.0;
            y /= 150.0;
            graphics.camera.rotate(y, -x);
            viewRotatingStartPos.set(mouseX, mouseY);

        }
        if (viewPanning) {
            int mouseX = mainFrame.getGraphicsWindowsMousePosX();
            int mouseY = mainFrame.getGraphicsWindowsMousePosY();
            if (mouseX == -1 || mouseY == -1)
                return;
            double x = mouseX - viewPanningStartPos.x;
            double y = mouseY - viewPanningStartPos.y;
            x /= 10.0;
            y /= 10.0;
            graphics.camera.pan(x, -y);
            viewPanningStartPos.set(mouseX, mouseY);
        }
        this.mainFrame.forceRepaint();
    }

    public void signalMouseClicked(int clickCount) {
        boolean doubleClick = false;
        if (clickCount == 2)
            doubleClick = true;
        if (state == State.SELECT) {
            // ***** picking *****
            Math3d.Double3 center;
            if (graphics.camera.mode == Camera.Mode.PERSPECTIVE)
                center = currentModel.pick(additiveSelection, doubleClick, graphics.camera.getEyePos(),
                        graphics.camera.rayDir);
            else
                center = currentModel.pick(additiveSelection, doubleClick, graphics.camera.intersectionNear,
                        graphics.camera.rayDir);
            if (center != null) {
                moveToolPos = center;
                renderMoveTool = true;
            } else
                renderMoveTool = false;

            this.mainFrame.forceRepaint();
        }
    }

    public void signalMousePressed() {
        if (state == State.INSERT) {
            state = State.INSERT_DRAGGING;
        }
    }

    public void signalMouseReleased() {
        if (state == State.INSERT_DRAGGING) {
            Model.Stone stone;
            for (Iterator<Model.Stone> it = currentStones.iterator(); it.hasNext();) {
                stone = (Model.Stone) it.next();
                this.currentModel.insertStone(-1, currentInsertID, stone.color, stone.type,
                        stone.pos.x, stone.pos.y, stone.pos.z, stone.rotation);
            }
            currentInsertID++;
            this.state = State.INSERT;
            this.mainFrame.forceRepaint();
        }
    }

    public void signalUnionSelection() {
        this.currentModel.unionSelection();
    }

    public void signalSeparateSelection() {
        this.currentModel.separateSelection(currentInsertID++);
    }

    public void signalKeyShift(boolean value) {
        holdHeight = false;
        additiveSelection = false;
        if (state == State.INSERT) {
            if (value)
                holdHeight = true;
        } else if (state == State.SELECT) {
            if (value)
                additiveSelection = true;
        }
        this.mainFrame.forceRepaint();
    }

    public void signalKeyAlt(boolean value) {
        viewRotating = value;
        if (viewRotating) {
            int mouseX = mainFrame.getGraphicsWindowsMousePosX();
            int mouseY = mainFrame.getGraphicsWindowsMousePosY();
            if (mouseX == -1 || mouseY == -1)
                return;
            viewRotatingStartPos.set(mouseX, mouseY);
        }
    }

    public void signalKeyCtrl(boolean value) {
        viewPanning = value;
        if (viewPanning) {
            int mouseX = mainFrame.getGraphicsWindowsMousePosX();
            int mouseY = mainFrame.getGraphicsWindowsMousePosY();
            if (mouseX == -1 || mouseY == -1)
                return;
            viewPanningStartPos.set(mouseX, mouseY);
        }
    }

    public void signalDeleteSelection() {
        if (state == State.SELECT) {
            currentModel.deleteSelected();
            mainFrame.forceRepaint();
        }
    }

    // BUG: deletes selected stones, if e. g. writing in textfield ((and pressing
    // backspace)
    public void signalKeyBackspace() {
        // signalDeleteSelection();
        // mainFrame.forceRepaint();
    }

    public void signalMouseWheel(int notches) {
        graphics.camera.zoom((double) notches / 2.0);
        this.mainFrame.forceRepaint();
    }

    public void signalEnableSelectionMode() {
        this.state = State.SELECT;
    }

    public void signalEnableInsertionMode() {
        this.state = State.INSERT;
    }

    public State getState() {
        return state;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void signalFillStoneRect(boolean value) {
        this.currentStonesRectFilled = value;
    }

    public void signalEdgeWall(boolean value) {
        this.currentStonesEdgeWall = value;
    }

    public void signalViewModeChanged(int modeIndex) {
        switch (modeIndex) {
            case 0:
                graphics.camera.mode = Camera.Mode.PERSPECTIVE;
                break;
            case 1:
                graphics.camera.mode = Camera.Mode.TOP;
                break;
            case 2:
                graphics.camera.mode = Camera.Mode.BOTTOM;
                break;
            case 3:
                graphics.camera.mode = Camera.Mode.FRONT;
                break;
            case 4:
                graphics.camera.mode = Camera.Mode.BACK;
                break;
            case 5:
                graphics.camera.mode = Camera.Mode.LEFT;
                break;
            case 6:
                graphics.camera.mode = Camera.Mode.RIGHT;
                break;
        }
        mainFrame.forceProjectionChange();
        mainFrame.forceRepaint();
    }

    public void signalMoveSelectedStones(Math3d.Int3 transformation) {
        currentModel.moveSelectedStones(transformation);
        mainFrame.forceRepaint();
    }

    public void signalRotateViewY(double angle) {
        angle = angle / 360.0 * 2.0 * Math.PI;
        mainFrame.graphics.camera.rotate(0.0, angle);
        mainFrame.forceRepaint();
    }

    public void signalRotateViewXZ(double angle) {
        angle = angle / 360.0 * 2.0 * Math.PI;
        mainFrame.graphics.camera.rotate(angle, 0.0);
        mainFrame.forceRepaint();
    }

    public void signalPanView(double xz, double y) {
        graphics.camera.pan(xz, y);
        mainFrame.forceRepaint();
    }

    public void signalClearEverything() {
        currentModel = new Model(mainFrame.graphics);
        mainFrame.forceRepaint();
    }

    public boolean signalLoadFromFile(File file) throws IOException {
        signalClearEverything();

        FileInputStream fileIn = new FileInputStream(file);
        DataInputStream dataIn = new DataInputStream(fileIn);

        // read identifier
        if (dataIn.readInt() != 0x424C4353) {
            System.out.println("wrong file type");
            dataIn.close();
            fileIn.close();
            return false;
        }
        // read file-format-version
        if (dataIn.readInt() != 0x01) {
            System.out.println("wrong file-format-version");
            dataIn.close();
            fileIn.close();
            return false;
        }

        // read current insert id
        currentInsertID = dataIn.readInt();

        // read number of models
        // TBD: SUPPORT MORE THAN ONE MODELS

        // read current model
        currentModel.loadFromFile(0x01, dataIn);

        dataIn.close();
        fileIn.close();

        mainFrame.forceRepaint();

        return true;
    }

    public void signalSaveToFile(File file) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        DataOutputStream dataOut = new DataOutputStream(fileOut);

        // write identifier
        dataOut.writeInt(0x424C4353); // "BLCS" - ASCII
        // write file-format-version
        dataOut.writeInt(0x01);

        // write current insert id
        dataOut.writeInt(currentInsertID);

        // write number of models
        // dataOut.writeInt(mainFrame.models.size());

        // TBD: SUPPORT MORE THAN ONE MODEL

        // write current model
        currentModel.saveToFile(0x01, dataOut);

        dataOut.close();
        fileOut.close();
    }

}
