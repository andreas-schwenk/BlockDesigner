/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GuiButtonListener implements ActionListener, ItemListener, ChangeListener, ListSelectionListener {
    private GuiMainFrame mainFrame;

    public class MoveDialog extends JDialog {
        JButton jbOK;

        MoveDialog() {
            // super("move");
            Container container = getContentPane();
            jbOK = new JButton("OK");
            container.add(jbOK);
            pack();
            setVisible(true);
        }
    }

    private class MyFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith("blcs"))
                return true;
            return false;
        }

        @Override
        public String getDescription() {
            return "accept only .blcs files";
        }
    }

    private Math3d.Int3 getInt3FromDialog(String caption) {
        String vector = JOptionPane.showInputDialog(caption);
        Math3d.Int3 transformation = new Math3d.Int3();
        boolean error = false;
        try {
            StringTokenizer strTk = new StringTokenizer(vector);
            int numTokens = strTk.countTokens();
            for (int i = 0; i < numTokens; i++) {
                int tmp = Integer.parseInt(strTk.nextToken());
                switch (i) {
                    case 0:
                        transformation.x = tmp;
                        break;
                    case 1:
                        transformation.y = tmp;
                        break;
                    case 2:
                        transformation.z = tmp;
                        break;
                    default:
                        error = true;
                }
            }
        } catch (Exception exception) {
            error = true;
        }
        if (!error) {
            return transformation;
        }
        return null;
    }

    GuiButtonListener(GuiMainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    void refreshJcSelectionGroup() {
        mainFrame.jcSelectionGroup.removeAllItems();
        LinkedList<Model.SelectionGroup> selectionGroups = mainFrame.getGuiFSMReference().getSelectionGroups();
        Model.SelectionGroup grp = null;
        // ItemListener tmpItemListener =
        // mainFrame.jcSelectionGroup.getItemListeners()[0];
        ActionListener tmpActionListener = mainFrame.jcSelectionGroup.getActionListeners()[0];
        // mainFrame.jcSelectionGroup.removeItemListener(tmpItemListener);
        mainFrame.jcSelectionGroup.removeActionListener(tmpActionListener);
        for (Iterator<Model.SelectionGroup> it = selectionGroups.iterator(); it.hasNext();) {
            grp = (Model.SelectionGroup) it.next();
            mainFrame.jcSelectionGroup.addItem(grp.name);
        }
        mainFrame.jcSelectionGroup.setSelectedItem(grp.name);
        // mainFrame.jcSelectionGroup.addItemListener(tmpItemListener);
        mainFrame.jcSelectionGroup.addActionListener(tmpActionListener);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == mainFrame.jbInsert) {
            mainFrame.jbInsert.setSelected(true);
            mainFrame.jbSelect.setSelected(false);
            mainFrame.getGuiFSMReference().signalEnableInsertionMode();
        } else if (src == mainFrame.jbSelect) {
            mainFrame.jbSelect.setSelected(true);
            mainFrame.jbInsert.setSelected(false);
            mainFrame.getGuiFSMReference().signalEnableSelectionMode();
        } else if (src == mainFrame.jbRotateCurrentStone) {
            mainFrame.getGuiFSMReference().signalRotateCurrentStone();
        } else if (src == mainFrame.jrRoom) {
            mainFrame.getGuiFSMReference().signalFillStoneRect(false);
            mainFrame.getGuiFSMReference().signalEdgeWall(false);
        } else if (src == mainFrame.jrWall) {
            mainFrame.getGuiFSMReference().signalFillStoneRect(false);
            mainFrame.getGuiFSMReference().signalEdgeWall(true);
        } else if (src == mainFrame.jrSlab) {
            mainFrame.getGuiFSMReference().signalFillStoneRect(true);
            mainFrame.getGuiFSMReference().signalEdgeWall(true);
        } else if (src == mainFrame.jbDeleteSelection) {
            mainFrame.getGuiFSMReference().signalDeleteSelection();
        } else if (src == mainFrame.jbMoveSelection) {
            Math3d.Int3 transformation = getInt3FromDialog("enter vector (x y z): e. g.: \"1 0 -1\"");
            if (transformation != null)
                mainFrame.getGuiFSMReference().signalMoveSelectedStones(transformation);
        } else if (src == mainFrame.jbDuplicateSelection) {
            Math3d.Int3 transformation = getInt3FromDialog("enter vector (x y z): e. g.: \"1 0 -1\"");
            if (transformation != null)
                mainFrame.getGuiFSMReference().signalDuplicateSelection(transformation);
        } else if (src == mainFrame.jbUnionSelection) {
            mainFrame.getGuiFSMReference().signalUnionSelection();
        } else if (src == mainFrame.jbSeparateSelection) {
            mainFrame.getGuiFSMReference().signalSeparateSelection();
        } else if (src == mainFrame.jbGroupSelection) {
            String grpName = JOptionPane.showInputDialog("new group name:");
            if (grpName != null) {
                mainFrame.getGuiFSMReference().signalMakeGroupSelection(grpName);
                refreshJcSelectionGroup();
            }
        } else if (src == mainFrame.jbRotateViewLeft) {
            mainFrame.getGuiFSMReference().signalRotateViewY(-10.0);
        } else if (src == mainFrame.jbRotateViewRight) {
            mainFrame.getGuiFSMReference().signalRotateViewY(+10.0);
        } else if (src == mainFrame.jbRotateViewUp) {
            mainFrame.getGuiFSMReference().signalRotateViewXZ(10.0);
        } else if (src == mainFrame.jbRotateViewDown) {
            mainFrame.getGuiFSMReference().signalRotateViewXZ(-10.0);
        } else if (src == mainFrame.jbMoveViewLeft) {
            mainFrame.getGuiFSMReference().signalPanView(10.0, 0.0);
        } else if (src == mainFrame.jbMoveViewRight) {
            mainFrame.getGuiFSMReference().signalPanView(-10.0, 0.0);
        } else if (src == mainFrame.jbMoveViewUp) {
            mainFrame.getGuiFSMReference().signalPanView(0.0, -10.0);
        } else if (src == mainFrame.jbMoveViewDown) {
            mainFrame.getGuiFSMReference().signalPanView(0.0, 10.0);
        } else if (src == mainFrame.jbNew) {
            mainFrame.jcSelectionGroup.removeAllItems();
            mainFrame.getGuiFSMReference().signalClearEverything();
        } else if (src == mainFrame.jbLoad) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileChooser.addChoosableFileFilter(new MyFileFilter());
                try {
                    mainFrame.jcSelectionGroup.removeAllItems();
                    mainFrame.getGuiFSMReference().signalLoadFromFile(file);
                    refreshJcSelectionGroup();
                } catch (IOException exception) {
                }
            }
        } else if (src == mainFrame.jbSave) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileChooser.addChoosableFileFilter(new MyFileFilter());
                try {
                    mainFrame.getGuiFSMReference().signalSaveToFile(file);
                } catch (IOException exception) {
                }
            }
        } else if (src == mainFrame.jcSelectionGroup) {
            int selectedIndex = mainFrame.jcSelectionGroup.getSelectedIndex();
            LinkedList<Model.SelectionGroup> selectionGroups = mainFrame.getGuiFSMReference().getSelectionGroups();
            int i = 0;
            Model.SelectionGroup grp = null;
            for (Iterator<Model.SelectionGroup> it = selectionGroups.iterator(); it.hasNext();) {
                grp = (Model.SelectionGroup) it.next();
                if (i == selectedIndex) {
                    mainFrame.getGuiFSMReference().signalSelectByGroupID(grp.id);
                    break;
                }
                i++;
            }
        }
    }

    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        if (src == mainFrame.jcStoneColor) {
            mainFrame.getGuiFSMReference().currentStonesColor = mainFrame.jcStoneColor.getSelectedIndex();
        }
        /*
         * else if(src == mainFrame.jcSelectionGroup)
         * {
         * int selectedIndex = mainFrame.jcSelectionGroup.getSelectedIndex();
         * LinkedList<Model.SelectionGroup> selectionGroups =
         * mainFrame.getGuiFSMReference().getSelectionGroups();
         * int i=0;
         * Model.SelectionGroup grp = null;
         * for(Iterator it=selectionGroups.iterator(); it.hasNext(); )
         * {
         * grp = (Model.SelectionGroup)it.next();
         * if(i == selectedIndex)
         * {
         * mainFrame.getGuiFSMReference().signalSelectByGroupID(grp.id);
         * break;
         * }
         * i ++;
         * }
         * }
         */
        else if (src == mainFrame.jcViewMode) {
            int selectedIndex = mainFrame.jcViewMode.getSelectedIndex();
            if (mainFrame.getGuiFSMReference() != null)
                mainFrame.getGuiFSMReference().signalViewModeChanged(selectedIndex);
        }
    }

    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src == mainFrame.jsStackHeight) {
            int value = mainFrame.jsStackHeight.getValue();
            mainFrame.getGuiFSMReference().currentStonesStackHeight = value;
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        Object src = e.getSource();
        if (src == mainFrame.jlStoneType) {
            if (mainFrame.getGuiFSMReference() != null && mainFrame.graphics != null)
                mainFrame.getGuiFSMReference().signalSetCurrentStonesType(
                        mainFrame.graphics.obj3dStones.getSubId(
                                mainFrame.jlStoneType.getSelectedIndex()));
        }
    }

}
