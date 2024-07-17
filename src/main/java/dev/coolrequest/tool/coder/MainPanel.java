package dev.coolrequest.tool.coder;

import com.intellij.openapi.project.Project;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import dev.coolrequest.tool.CoolToolPanel;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel implements CoolToolPanel {
    private Project project;

    public MainPanel() {

    }

    /**
     * test code
     */
    public MainPanel(Project project) {
        this.project = project;
    }

    @Override
    public JPanel createPanel() {
        setLayout(new BorderLayout());
        JBTabs jbTabs = new JBTabsImpl(project);

        TabInfo encoderTabInfo = new TabInfo(new EncoderView());
        encoderTabInfo.setText("Encoder");
        jbTabs.addTab(encoderTabInfo);

        TabInfo decoderTabInfo = new TabInfo(new DecoderView());
        decoderTabInfo.setText("Decoder");
        jbTabs.addTab(decoderTabInfo);
        add(jbTabs.getComponent(), BorderLayout.CENTER);
        return this;
    }

    @Override
    public void showTool() {

    }

    @Override
    public void closeTool() {

    }
}
