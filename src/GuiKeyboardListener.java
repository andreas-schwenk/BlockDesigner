/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.awt.*;
import java.awt.event.*;

public class GuiKeyboardListener implements KeyEventDispatcher {
    GuiMainFrame mainFrame;
    GuiFSM guiFSM;

    GuiKeyboardListener(GuiMainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        this.guiFSM = mainFrame.getGuiFSMReference();
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        int keyCodeChar = e.getKeyChar();
        int keyCode = e.getKeyCode();

        boolean discard = false;
        if (e.getID() == KeyEvent.KEY_TYPED) {
        }

        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (keyCode) {
                // move
                case KeyEvent.VK_LEFT:
                    guiFSM.signalKeyArrowLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    guiFSM.signalKeyArrowRight();
                    break;
                case KeyEvent.VK_UP:
                    guiFSM.signalKeyArrowUp();
                    break;
                case KeyEvent.VK_DOWN:
                    guiFSM.signalKeyArrowDown();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    guiFSM.signalKeyPageUp();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    guiFSM.signalKeyPageDown();
                    break;
                case KeyEvent.VK_0:
                    guiFSM.signalKeyNumber(0);
                    break;
                case KeyEvent.VK_1:
                    guiFSM.signalKeyNumber(1);
                    break;
                case KeyEvent.VK_2:
                    guiFSM.signalKeyNumber(2);
                    break;
                case KeyEvent.VK_3:
                    guiFSM.signalKeyNumber(3);
                    break;
                case KeyEvent.VK_4:
                    guiFSM.signalKeyNumber(4);
                    break;
                case KeyEvent.VK_5:
                    guiFSM.signalKeyNumber(5);
                    break;
                case KeyEvent.VK_6:
                    guiFSM.signalKeyNumber(6);
                    break;
                case KeyEvent.VK_7:
                    guiFSM.signalKeyNumber(7);
                    break;
                case KeyEvent.VK_8:
                    guiFSM.signalKeyNumber(8);
                    break;
                case KeyEvent.VK_9:
                    guiFSM.signalKeyNumber(9);
                    break;
                case KeyEvent.VK_ALT:
                    guiFSM.signalKeyAlt(true);
                    break;
                case KeyEvent.VK_CONTROL:
                    guiFSM.signalKeyCtrl(true);
                    break;
                case KeyEvent.VK_SHIFT:
                    guiFSM.signalKeyShift(true);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    guiFSM.signalKeyBackspace();
                    break;
                case KeyEvent.VK_R:
                    guiFSM.signalKeyLetter('R');
                    break;
            }

            switch (keyCodeChar) {
                /*
                 * case (int)'+':
                 * this.mainFrame.getGuiFSM().signalZoom(true);
                 * break;
                 * case (int)'-':
                 * this.mainFrame.getGuiFSM().signalZoom(false);
                 * break;
                 */
            }
        }

        if (e.getID() == KeyEvent.KEY_RELEASED) {
            switch (keyCode) {
                case KeyEvent.VK_ALT:
                    guiFSM.signalKeyAlt(false);
                    break;
                case KeyEvent.VK_CONTROL:
                    guiFSM.signalKeyCtrl(false);
                    break;
                case KeyEvent.VK_SHIFT:
                    guiFSM.signalKeyShift(false);
                    break;
            }
        }
        return discard;
    }
}