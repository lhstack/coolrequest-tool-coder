package dev.coolrequest.tool.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ComponentUtils {

    public static JPanel createJustifiedPanel(JComponent left, JComponent right) {
        JPanel jPanel = new JPanel(new GridLayout(1, 2));
        jPanel.add(createFlowLayoutPanel(left, FlowLayout.LEFT));
        jPanel.add(createFlowLayoutPanel(right, FlowLayout.RIGHT));
        return jPanel;
    }

    public static JPanel createBorderedPanel(JComponent top, JComponent body) {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(createFlowLayoutPanel(top, FlowLayout.LEFT), BorderLayout.NORTH);
        jPanel.add(body, BorderLayout.CENTER);
        return jPanel;
    }


    //创建FlowLayout布局面板
    public static JPanel createFlowLayoutPanel(JComponent component, int layout) {
        JPanel jPanel = new JPanel(new FlowLayout(layout));
        jPanel.add(component);
        return jPanel;
    }

    public static JPanel createFlowLayoutPanel(int layout, JComponent... components) {
        JPanel jPanel = new JPanel(new FlowLayout(layout));
        for (JComponent component : components) {
            jPanel.add(component);
        }
        return jPanel;
    }

    public static void addMouseClickListener(JComponent component, Consumer<MouseEvent> listener) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    listener.accept(e);
                }
            }
        });
    }

}
