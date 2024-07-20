package dev.coolrequest.tool.components;

import javax.swing.*;
import java.awt.*;

public class SimpleDialog extends JDialog {
    private final JComponent panel;

    public SimpleDialog(String title, JComponent panel) {
        this.panel = panel;
        this.setTitle(title);
        this.setSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.init();
    }

    private void init() {
        this.getContentPane().add(this.panel);
    }


}
