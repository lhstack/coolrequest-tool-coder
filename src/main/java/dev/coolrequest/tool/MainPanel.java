package dev.coolrequest.tool;

import com.intellij.openapi.project.Project;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import dev.coolrequest.tool.coder.CoderView;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.script.ScriptView;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel implements CoolToolPanel {
    private Project project;

    @Override
    public JPanel createPanel() {
        setLayout(new BorderLayout());
        JBTabs jbTabs = new JBTabsImpl(project);

        TabInfo encoderTabInfo = new TabInfo(new CoderView(project));
        encoderTabInfo.setText(I18n.getString("coder.title"));
        jbTabs.addTab(encoderTabInfo);

        TabInfo decoderTabInfo = new TabInfo(new ScriptView(project));
        decoderTabInfo.setText(I18n.getString("script.title"));
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
