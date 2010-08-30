/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.io.*;
import java.util.*;

import com.jogamp.opengl.*;

public class Model {
    private int currentSelectionGroupID = 0;
    private LinkedList<SelectionGroup> selectionGroups = new LinkedList<SelectionGroup>();

    private Graphics graphics;

    private Math3d.Double3 pos = new Math3d.Double3(0, 0, 0);
    private LinkedList<Stone> stones = null;

    private int currentStoneID = 0;

    public static class Stone {
        public int id;
        public int insertID;
        public int color = 0;
        public int type;
        public int rotation = 0;
        public Math3d.Int3 pos = new Math3d.Int3(0, 0, 0);
        public boolean selected = false;
        public Math3d.AABB aabb = null;
        private int obj3dIndex = 0; // increases performance

        public Stone() {
        }

        public static int getSizeX(int type) {
            return type / 100;
        }

        public static int getSizeY(int type) {
            return (type / 10) % 10;
        }

        public static int getSizeZ(int type) {
            return (type / 1) % 10;
        }

        @Override
        public Stone clone() {
            Stone stone = new Stone();
            stone.insertID = insertID;
            stone.color = color;
            stone.type = type;
            stone.obj3dIndex = obj3dIndex;
            stone.rotation = rotation;
            if (pos != null)
                stone.pos = pos.clone();
            stone.selected = selected;
            if (aabb != null)
                stone.aabb = aabb.clone();
            return stone;
        }
    }

    public class SelectionGroup {
        public int id;
        public String name = "";
        public LinkedList<Stone> stones = null;
    }

    public Model(Graphics graphics) {
        this.graphics = graphics;
        this.stones = new LinkedList<Stone>();
    }

    public void render(GL2 gl) {
        Stone stone;
        for (Iterator<Stone> it = this.stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected)
                gl.glColor3d(1.0, 0.0, 1.0);
            else
                GraphicsHelper.setColor(gl, stone.color);
            graphics.obj3dStones.renderBySubIndex(gl, stone.obj3dIndex, pos.x + stone.pos.x, pos.y + 0.4 * stone.pos.y,
                    pos.z + stone.pos.z, 0.0, stone.rotation * 90.0, 0.0);
        }
    }

    public void setPosition(double x, double y, double z) {
        this.pos.x = x;
        this.pos.x = y;
        this.pos.x = z;
    }

    /**
     * submit -1 for id to create a new one
     */
    public void insertStone(int id, int insertID, int color, int type, int x, int y, int z, int rotation) {
        Stone newStone = new Stone();
        if (id == -1)
            newStone.id = currentStoneID++;
        else
            newStone.id = id;
        newStone.insertID = insertID;
        newStone.color = color;
        newStone.type = type;
        newStone.obj3dIndex = graphics.obj3dStones.getSubIndexFromSubId(type);
        newStone.pos.x = x;
        newStone.pos.y = y;
        newStone.pos.z = z;
        newStone.rotation = rotation;
        newStone.aabb = graphics.obj3dStones.getAABB(type, 0, newStone.rotation * 90, 0);
        Stone stone;
        Math3d.AABB a = new Math3d.AABB(), b = new Math3d.AABB();
        a.min.x = newStone.aabb.min.x + newStone.pos.x + 0.01; // add 0.01 to prevent numerical instability
        a.min.y = newStone.aabb.min.y + newStone.pos.y * 0.4 + 0.01;
        a.min.z = newStone.aabb.min.z + newStone.pos.z + 0.01;
        a.max.x = newStone.aabb.max.x + newStone.pos.x - 0.01;
        a.max.y = newStone.aabb.max.y + newStone.pos.y * 0.4 - 0.01;
        a.max.z = newStone.aabb.max.z + newStone.pos.z - 0.01;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            b.min.x = stone.aabb.min.x + stone.pos.x + 0.01;
            b.min.y = stone.aabb.min.y + stone.pos.y * 0.4 + 0.01;
            b.min.z = stone.aabb.min.z + stone.pos.z + 0.01;
            b.max.x = stone.aabb.max.x + stone.pos.x - 0.01;
            b.max.y = stone.aabb.max.y + stone.pos.y * 0.4 - 0.01;
            b.max.z = stone.aabb.max.z + stone.pos.z - 0.01;
            if (a.collision(b))
                return;
        }
        this.stones.add(newStone);
    }

    public void saveToFile(int fileVersion, DataOutputStream dataOut) throws IOException {
        // output position
        dataOut.writeDouble(pos.x);
        dataOut.writeDouble(pos.y);
        dataOut.writeDouble(pos.z);

        // write stones
        // number of stones
        dataOut.writeInt(stones.size());
        // stones
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            dataOut.writeInt(stone.id);
            dataOut.writeInt(stone.insertID);
            dataOut.writeInt(stone.color);
            dataOut.writeInt(stone.type);
            dataOut.writeInt(stone.rotation);
            dataOut.writeInt(stone.pos.x);
            dataOut.writeInt(stone.pos.y);
            dataOut.writeInt(stone.pos.z);
        }
        // current stone id
        dataOut.writeInt(currentStoneID);

        // write selection groups
        // number of selection groups
        dataOut.writeInt(selectionGroups.size());
        // current selection group ID
        dataOut.writeInt(currentSelectionGroupID);
        // selection groups
        SelectionGroup grp;
        for (Iterator<SelectionGroup> it = selectionGroups.iterator(); it.hasNext();) {
            grp = (SelectionGroup) it.next();
            dataOut.writeInt(grp.id);
            dataOut.writeUTF(grp.name);
            // write appropriate stones
            dataOut.writeInt(grp.stones.size());
            for (Iterator<Stone> it2 = grp.stones.iterator(); it2.hasNext();) {
                stone = (Stone) it2.next();
                dataOut.writeInt(stone.id);
            }
        }
    }

    public void loadFromFile(int fileVersion, DataInputStream dataIn) throws IOException {
        // empty existing lists
        selectionGroups = new LinkedList<SelectionGroup>();
        stones = new LinkedList<Stone>();

        // input position
        pos.x = dataIn.readDouble();
        pos.y = dataIn.readDouble();
        pos.z = dataIn.readDouble();

        // read stones
        // number of stones
        int numStones = dataIn.readInt();
        // stones
        int id, insertID, color, type, rotation, posX, posY, posZ;
        for (int i = 0; i < numStones; i++) {
            id = dataIn.readInt();
            insertID = dataIn.readInt();
            color = dataIn.readInt();
            type = dataIn.readInt();
            rotation = dataIn.readInt();
            posX = dataIn.readInt();
            posY = dataIn.readInt();
            posZ = dataIn.readInt();
            insertStone(id, insertID, color, type, posX, posY, posZ, rotation);
        }
        // current stone id
        currentStoneID = dataIn.readInt();

        // read selection groups
        // number of selection groups
        int numSelGroups = dataIn.readInt();
        // current selection group id
        currentSelectionGroupID = dataIn.readInt();
        // selection groups
        for (int i = 0; i < numSelGroups; i++) {
            SelectionGroup grp = new SelectionGroup();
            grp.id = dataIn.readInt();
            grp.name = dataIn.readUTF();
            grp.stones = new LinkedList<Stone>();
            // read appropriate stones
            int numGrpStones = dataIn.readInt();
            int grpStoneId;
            for (int j = 0; j < numGrpStones; j++) {
                grpStoneId = dataIn.readInt();
                // create link
                // a) search in stone-list
                Stone tmp;
                for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
                    tmp = (Stone) it.next();
                    if (tmp.id == grpStoneId) {
                        // b) insert
                        grp.stones.add(tmp);
                        break;
                    }
                }
            }
            selectionGroups.add(grp);
        }
    }

    private int lastHeight = 0;

    public Math3d.Int3 getPlacementPos(Math3d.Double3 eyePos, Math3d.Double3 rayDir,
            int gridHeight, boolean holdHeightForPlacement) {
        Math3d.Int3 ret = new Math3d.Int3();
        if (holdHeightForPlacement) {
            Math3d.getIntersection(eyePos, rayDir, ret, lastHeight * 0.4);
            ret.y = lastHeight;
            return ret;
        }
        double distance;
        // grid
        distance = Math3d.getIntersection(eyePos, rayDir, ret, gridHeight * 0.4);
        ret.y = gridHeight;
        // stones
        Math3d.Double3 tmpRet = new Math3d.Double3();
        double tmpDistance;
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            tmpDistance = Math3d.getIntersection(
                    eyePos, rayDir, tmpRet, stone.pos.y * 0.4 + stone.aabb.max.y);
            if (tmpDistance < distance &&
                    tmpRet.x >= (pos.x + stone.pos.x + stone.aabb.min.x)
                    && tmpRet.x <= (pos.x + stone.pos.x + stone.aabb.max.x) &&
                    tmpRet.z >= (pos.z + stone.pos.z + stone.aabb.min.z)
                    && tmpRet.z <= (pos.z + stone.pos.z + stone.aabb.max.z)) {
                distance = tmpDistance;
                ret.x = (int) Math.floor(tmpRet.x);
                ret.y = (int) stone.pos.y + (int) ((stone.aabb.max.y + 0.01) / 0.4); // add 0.01 to avoid numerical
                                                                                     // instability
                ret.z = (int) Math.floor(tmpRet.z);
            }
        }
        // ret
        lastHeight = ret.y;
        return ret;
    }

    /**
     * returns center of selection (e. g. for move-tool); if no selection: null is
     * returned
     * 
     * @param additive
     * @param allWithSameInsertID
     * @param eyePos
     * @param rayDir
     * @return
     */
    public Math3d.Double3 pick(boolean additive, boolean allWithSameInsertID,
            Math3d.Double3 eyePos, Math3d.Double3 rayDir) {
        Math3d.Double3 center = new Math3d.Double3();
        int numSelected = 0;
        Math3d.AABB aabb = new Math3d.AABB();
        double distance = Double.MAX_VALUE, tmpDistance;
        Stone stone, selectionCandidate = null;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (!additive)
                stone.selected = false;
            else if (stone.selected) {
                center.add(stone.pos);
                numSelected++;
            }

            aabb.min.set(stone.aabb.min);
            aabb.min.x += stone.pos.x;
            aabb.min.y += stone.pos.y * 0.4;
            aabb.min.z += stone.pos.z;

            aabb.max.set(stone.aabb.max);
            aabb.max.x += stone.pos.x;
            aabb.max.y += stone.pos.y * 0.4;
            aabb.max.z += stone.pos.z;

            if (((tmpDistance = aabb.rayIntersection(eyePos, rayDir)) > 0) && tmpDistance < distance) {
                distance = tmpDistance;
                selectionCandidate = stone;
            }
        }
        if (selectionCandidate != null) {
            selectionCandidate.selected = true;
            center.add(selectionCandidate.pos);
            numSelected++;
        }
        if (allWithSameInsertID && selectionCandidate != null) {
            for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
                stone = (Stone) it.next();
                if (stone.insertID == selectionCandidate.insertID && !stone.selected) {
                    stone.selected = true;
                    center.add(stone.pos);
                    numSelected++;
                }
            }
        }
        if (numSelected > 0) {
            center.x /= (double) numSelected;
            center.y /= (double) numSelected;
            center.z /= (double) numSelected;
            center.y *= 0.4;
            center.y += 0.3;
            return center;
        }
        return null;
    }

    public void deleteSelected() {
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected)
                it.remove();
        }
    }

    public LinkedList<Model.Stone> getSelectedStones() {
        LinkedList<Model.Stone> ret = new LinkedList<Model.Stone>();

        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected)
                ret.add(stone);
        }
        return ret;
    }

    /**
     * make insertID of all selected stones the same
     */
    public void unionSelection() {
        boolean first = true;
        int insertID = 0;
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected) {
                if (first) {
                    first = false;
                    insertID = stone.insertID;
                } else {
                    if (stone.insertID != insertID)
                        stone.insertID = insertID;
                }
            }
        }
    }

    /**
     * transfer a new insertID to selected stones
     * 
     * @param newInsertID
     */
    public void separateSelection(int newInsertID) {
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected)
                stone.insertID = newInsertID;
        }
    }

    /**
     * 
     * @param grpName
     * @return group-ID
     */
    public int makeGroupFromSelection(String grpName) {
        SelectionGroup grp = new SelectionGroup();
        grp.id = currentSelectionGroupID++;
        grp.name = grpName;
        grp.stones = new LinkedList<Model.Stone>();
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected)
                grp.stones.add(stone);
        }
        selectionGroups.add(grp);
        return grp.id;
    }

    public void deleteGroupSelection(int id) {
        SelectionGroup grp;
        for (Iterator<SelectionGroup> it = selectionGroups.iterator(); it.hasNext();) {
            grp = (SelectionGroup) it.next();
            if (grp.id == id) {
                it.remove();
                return;
            }
        }
    }

    public LinkedList<SelectionGroup> getSelectionGroups() {
        return selectionGroups;
    }

    public void deselectAll() {
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            stone.selected = false;
        }
    }

    public void selectByGroupID(int id) {
        deselectAll();
        SelectionGroup grp;
        for (Iterator<SelectionGroup> it = selectionGroups.iterator(); it.hasNext();) {
            grp = (SelectionGroup) it.next();
            if (grp.id == id) {
                Stone stone;
                for (Iterator<Stone> it2 = grp.stones.iterator(); it2.hasNext();) {
                    stone = (Stone) it2.next();
                    stone.selected = true;
                }
                break;
            }
        }
    }

    public void moveSelectedStones(Math3d.Int3 transformation) {
        LinkedList<Stone> toChange = new LinkedList<Stone>();
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected) {
                toChange.add(stone);
                it.remove();
            }
        }
        for (Iterator<Stone> it = toChange.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            stone.pos.add(transformation);
            insertStone(-1, stone.insertID, stone.color, stone.type, stone.pos.x, stone.pos.y, stone.pos.z,
                    stone.rotation);
        }
    }

    public void duplicateSelectedStones(Math3d.Int3 transformation, int insertID) {
        Math3d.Int3 newPos = new Math3d.Int3();

        LinkedList<Stone> toCopy = new LinkedList<Stone>();
        Stone stone;
        for (Iterator<Stone> it = stones.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            if (stone.selected) {
                toCopy.add(stone);
            }
        }
        for (Iterator<Stone> it = toCopy.iterator(); it.hasNext();) {
            stone = (Stone) it.next();
            newPos.set(stone.pos);
            newPos.add(transformation);
            insertStone(-1, insertID, stone.color, stone.type, newPos.x, newPos.y, newPos.z, stone.rotation);
        }
    }

}
