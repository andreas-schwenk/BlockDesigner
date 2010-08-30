/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

public class GuiMainFrame extends JFrame {
    protected JPanel jpTop, jpLeft, jpGraphics;
    protected Graphics graphics;
    protected GLCanvas glCanvas;

    protected JButton jbDuplicateSelection, jbUnionSelection,
            jbSeparateSelection, jbGroupSelection, jbMoveSelection,
            jbRotateCurrentStone, jbDeleteSelection,
            jbRotateViewLeft, jbRotateViewRight, jbRotateViewUp, jbRotateViewDown,
            jbMoveViewLeft, jbMoveViewRight, jbMoveViewUp, jbMoveViewDown,
            jbNew, jbLoad, jbSave;
    protected JToggleButton jbSelect, jbInsert;
    protected JRadioButton jrRoom, jrWall, jrSlab;
    protected JList jlStoneType;
    protected DefaultListModel jlmStoneType;
    protected JComboBox jcStoneColor, jcSelectionGroup, jcViewMode;
    protected JSlider jsStackHeight;

    private GuiButtonListener guiButtonListener;
    private GuiKeyboardListener guiKeyboardListener;

    private GuiFSM guiFSM;

    protected LinkedList<Model> models = null;

    public GuiMainFrame(String caption) {
        super(caption);

        // ********** init model-array **********
        this.models = new LinkedList<Model>();

        guiButtonListener = new GuiButtonListener(this);

        Container container = this.getContentPane();
        this.setSize(1200, 850);

        this.setLayout(new BorderLayout());

        JPanel tmp = new JPanel();
        tmp.setLayout(new FlowLayout(FlowLayout.LEFT));
        tmp.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
        container.add(tmp, BorderLayout.NORTH);

        this.jpTop = new JPanel();
        // this.jpTop.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150),
        // 2));
        this.jpTop.setLayout(new BoxLayout(this.jpTop, BoxLayout.X_AXIS));
        // container.add(this.jpTop, BorderLayout.NORTH);
        tmp.add(jpTop);

        jbSelect = new JToggleButton("select");
        jbSelect.addActionListener(guiButtonListener);
        jpTop.add(jbSelect);

        jbInsert = new JToggleButton("insert", true);
        jbInsert.addActionListener(guiButtonListener);
        jpTop.add(jbInsert);

        jpTop.add(new JSeparator(JSeparator.VERTICAL));

        jpTop.add(new JLabel("selection: "));

        jbMoveSelection = new JButton("move");
        jbMoveSelection.addActionListener(guiButtonListener);
        jpTop.add(jbMoveSelection);

        jbDuplicateSelection = new JButton("duplicate");
        jbDuplicateSelection.addActionListener(guiButtonListener);
        jpTop.add(jbDuplicateSelection);

        jbDeleteSelection = new JButton("delete");
        jbDeleteSelection.addActionListener(guiButtonListener);
        jpTop.add(jbDeleteSelection);

        jbUnionSelection = new JButton("union");
        jbUnionSelection.addActionListener(guiButtonListener);
        jpTop.add(jbUnionSelection);

        jbSeparateSelection = new JButton("separate");
        jbSeparateSelection.addActionListener(guiButtonListener);
        jpTop.add(jbSeparateSelection);

        jbGroupSelection = new JButton("group");
        jbGroupSelection.addActionListener(guiButtonListener);
        jpTop.add(jbGroupSelection);

        jcSelectionGroup = new JComboBox() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(175, 25);
            }
        };
        // jcSelectionGroup.addItemListener(guiButtonListener);
        jcSelectionGroup.addActionListener(guiButtonListener);
        jpTop.add(jcSelectionGroup);

        jpTop.add(new JSeparator(JSeparator.VERTICAL));

        jpTop.add(new JLabel("view: "));

        jcViewMode = new JComboBox();
        jcViewMode.addItemListener(guiButtonListener);
        jcViewMode.addItem(("Perspective"));
        jcViewMode.addItem(("Top"));
        jcViewMode.addItem(("Bottom"));
        jcViewMode.addItem(("Front"));
        jcViewMode.addItem(("Back"));
        jcViewMode.addItem(("Left"));
        jcViewMode.addItem(("Right"));
        jpTop.add(jcViewMode);

        jpTop.add(new JSeparator(JSeparator.VERTICAL));

        tmp = new JPanel();
        tmp.setLayout(new FlowLayout());
        tmp.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
        container.add(tmp, BorderLayout.WEST);

        this.jpLeft = new JPanel();
        this.jpLeft.setLayout(new BoxLayout(jpLeft, BoxLayout.Y_AXIS));
        tmp.add(jpLeft);

        Object[] colors = { "white", "yellow", "red", "blue", "black", "green", "brown", "gray" };
        jcStoneColor = new JComboBox(colors);
        jcStoneColor.addItemListener(guiButtonListener);
        jpLeft.add(jcStoneColor);

        Box box = Box.createHorizontalBox();
        jpLeft.add(box);
        box.add(Box.createHorizontalStrut(10));

        jlmStoneType = new DefaultListModel();
        jlStoneType = new JList(jlmStoneType);
        jlStoneType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlStoneType.addListSelectionListener(guiButtonListener);
        JScrollPane scrollPane = new JScrollPane(jlStoneType,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(225, 250);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        box.add(scrollPane);

        box = Box.createHorizontalBox();
        jpLeft.add(box);

        jbRotateCurrentStone = new JButton("rotate");
        jbRotateCurrentStone.addActionListener(guiButtonListener);
        box.add(jbRotateCurrentStone);

        jpLeft.add(new JSeparator(JSeparator.HORIZONTAL));

        box = Box.createHorizontalBox();
        jpLeft.add(box);

        ButtonGroup jbFillMode = new ButtonGroup();
        jrRoom = new JRadioButton("room");
        jrRoom.addActionListener(guiButtonListener);
        jbFillMode.add(jrRoom);
        box.add(jrRoom);
        jrWall = new JRadioButton("wall");
        jrWall.addActionListener(guiButtonListener);
        jbFillMode.add(jrWall);
        box.add(jrWall);
        jrSlab = new JRadioButton("slab");
        jrSlab.addActionListener(guiButtonListener);
        jbFillMode.add(jrSlab);
        box.add(jrSlab);

        jpLeft.add(new JSeparator(JSeparator.HORIZONTAL));

        box = Box.createHorizontalBox();
        jpLeft.add(box);
        box.add(new JLabel("stack-height", JLabel.LEFT));

        box = Box.createHorizontalBox();
        jpLeft.add(box);

        jsStackHeight = new JSlider(1, 10);
        jsStackHeight.setSnapToTicks(true);
        jsStackHeight.setMajorTickSpacing(1);
        jsStackHeight.setPaintTicks(true);
        jsStackHeight.setPaintLabels(true);
        jsStackHeight.setPaintTrack(true);
        jsStackHeight.setValue(1);
        jsStackHeight.addChangeListener(guiButtonListener);
        box.add(jsStackHeight);

        jpLeft.add(new JSeparator(JSeparator.HORIZONTAL));

        box = Box.createHorizontalBox();
        jpLeft.add(box);
        box.add(new JLabel("view-rotation:"));
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbRotateViewUp = new JButton("↑");
        jbRotateViewUp.addActionListener(guiButtonListener);
        box.add(jbRotateViewUp);
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbRotateViewLeft = new JButton("←");
        jbRotateViewLeft.addActionListener(guiButtonListener);
        box.add(jbRotateViewLeft);
        jbRotateViewRight = new JButton("→");
        jbRotateViewRight.addActionListener(guiButtonListener);
        box.add(jbRotateViewRight);
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbRotateViewDown = new JButton("↓");
        jbRotateViewDown.addActionListener(guiButtonListener);
        box.add(jbRotateViewDown);

        box = Box.createHorizontalBox();
        jpLeft.add(box);
        box.add(new JLabel("view-transformation:"));
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbMoveViewUp = new JButton("↑");
        jbMoveViewUp.addActionListener(guiButtonListener);
        box.add(jbMoveViewUp);
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbMoveViewLeft = new JButton("←");
        jbMoveViewLeft.addActionListener(guiButtonListener);
        box.add(jbMoveViewLeft);
        jbMoveViewRight = new JButton("→");
        jbMoveViewRight.addActionListener(guiButtonListener);
        box.add(jbMoveViewRight);
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbMoveViewDown = new JButton("↓");
        jbMoveViewDown.addActionListener(guiButtonListener);
        box.add(jbMoveViewDown);

        jpLeft.add(new JSeparator(JSeparator.HORIZONTAL));
        jpLeft.add(new JLabel(" "));
        jpLeft.add(new JLabel(" "));
        box = Box.createHorizontalBox();
        jpLeft.add(box);
        jbNew = new JButton("new");
        jbNew.addActionListener(guiButtonListener);
        box.add(jbNew);
        jbLoad = new JButton("load");
        jbLoad.addActionListener(guiButtonListener);
        box.add(jbLoad);
        jbSave = new JButton("save");
        jbSave.addActionListener(guiButtonListener);
        box.add(jbSave);

        this.jpGraphics = new JPanel();
        this.jpGraphics.setLayout(new GridLayout(1, 1));
        this.jpGraphics.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
        this.jpGraphics.setMinimumSize(new Dimension(100, 100));
        container.add(this.jpGraphics, BorderLayout.CENTER);

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities glCap = new GLCapabilities(profile);
        glCap.setDoubleBuffered(true);
        glCap.setNumSamples(4);
        // glCap.setStencilBits(2);
        glCap.setSampleBuffers(true);
        this.glCanvas = new GLCanvas(glCap);
        this.graphics = new Graphics(this);

        for (int i = 0; i < graphics.obj3dStones.getSubLength(); i++)
            jlmStoneType.addElement(graphics.obj3dStones.getSubCaption(i));
        jlStoneType.setSelectedIndex(2);

        this.jpGraphics.add(glCanvas);

        // ********** layout-management **********
        // align frame to center of screen
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point();
        location.x = screenSize.width / 2 - this.getWidth() / 2;
        location.y = screenSize.height / 3 - this.getHeight() / 3;
        // set location and visible
        this.setLocation(location);
        this.setVisible(true);

        // ********** gui-finite-state-machine **********
        this.guiFSM = new GuiFSM(this);

        // ********** keyboard-listener **********
        this.guiKeyboardListener = new GuiKeyboardListener(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(guiKeyboardListener);

        // ********** mouse-listener **********
        GuiMouseListener guiMouseListener = new GuiMouseListener(this);
        this.glCanvas.addMouseListener(guiMouseListener);
        this.glCanvas.addMouseMotionListener(guiMouseListener);
        this.glCanvas.addMouseWheelListener(guiMouseListener);

        // ********** painting not allowed until here **********
        this.glCanvas.addGLEventListener(this.graphics);
    }

    public float getDisplayScale() {
        int pixelWidth = this.glCanvas.getSurfaceWidth();
        int logicalWidth = this.glCanvas.getWidth();
        float scale = (float) pixelWidth / (float) logicalWidth;
        return scale;
    }

    public int getGraphicsWindowsMousePosX() {
        Point point = this.glCanvas.getMousePosition();
        if (point != null)
            return (int) (point.x * this.getDisplayScale());
        else
            return -1;
    }

    public int getGraphicsWindowsMousePosY() {
        Point point = this.glCanvas.getMousePosition();
        if (point != null)
            return (int) (point.y * this.getDisplayScale());
        else
            return -1;
    }

    public void forceRepaint() {
        this.glCanvas.repaint();
    }

    public void forceProjectionChange() {
        this.glCanvas.reshape(glCanvas.getX(), glCanvas.getY(), glCanvas.getWidth(), glCanvas.getHeight());
    }

    public Graphics getGraphicsReference() {
        return this.graphics;
    }

    public GuiFSM getGuiFSMReference() {
        return this.guiFSM;
    }

    public static void main(String[] args) {
        GuiMainFrame guiMainFrame = new GuiMainFrame("BlockDesigner");
    }

    public Model addModel() {
        Model model = new Model(this.graphics);
        this.models.add(model);
        return model;
    }
}
