package dev.coolrequest.tool.components;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PopupMenu extends JPopupMenu {

    public static PopupMenu create(JMenuItem... items) {
        PopupMenu popupMenu = new PopupMenu();
        for (JMenuItem item : items) {
            popupMenu.add(item);
        }
        return popupMenu;
    }

    public static void attachClearMenu(String title, Icon icon, JTextArea textArea){
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.add(new JMenuItem(title,icon){
            @Override
            protected void processMouseEvent(MouseEvent e) {
                if (e.getID() == MouseEvent.MOUSE_RELEASED && SwingUtilities.isLeftMouseButton(e)) {
                    textArea.setText("");
                }
                super.processMouseEvent(e);
            }
        });
        textArea.addMouseListener(popupMenu.createRightClickMouseAdapter());
    }

    public void rightClickShow(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            this.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public MouseAdapter createRightClickMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PopupMenu.this.rightClickShow(e);
            }
        };
    }
}
