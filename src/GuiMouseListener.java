/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.awt.event.*;

class GuiMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
    GuiMainFrame mainFrame;

    GuiMouseListener(GuiMainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void mouseDragged(MouseEvent e) {
        this.mainFrame.getGuiFSMReference().signalMouseMoved();
    }

    public void mouseMoved(MouseEvent e) {
        this.mainFrame.getGuiFSMReference().signalMouseMoved();
    }

    public void mouseClicked(MouseEvent e) {
        this.mainFrame.getGuiFSMReference().signalMouseClicked(e.getClickCount());
    }

    public void mousePressed(MouseEvent e) {
        this.mainFrame.getGuiFSMReference().signalMousePressed();
    }

    public void mouseReleased(MouseEvent e) {
        this.mainFrame.getGuiFSMReference().signalMouseReleased();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        this.mainFrame.getGuiFSMReference().signalMouseWheel(notches);
    }
}
