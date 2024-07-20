package dev.coolrequest.tool.components;

import javax.swing.*;
import java.awt.*;

public class SimpleFrame extends JFrame {
    private final JComponent panel;

    public SimpleFrame(JComponent panel, String title, Dimension initialSize) {
        this.panel = panel;
        this.setSize(initialSize);
        this.setTitle(title);
        this.init();
    }

    private void init() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setContentPane(this.panel);
    }
}
