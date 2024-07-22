package dev.coolrequest.tool;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.views.coder.CoderView;
import dev.coolrequest.tool.views.log.LogView;
import dev.coolrequest.tool.views.script.ScriptView;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel implements CoolToolPanel {
    private Project project;

    @Override
    public JPanel createPanel() {
        setLayout(new BorderLayout());
        try {
            JBTabs jbTabs = new JBTabsImpl(project);

            TabInfo encoderTabInfo = new TabInfo(new CoderView(project));
            encoderTabInfo.setText(I18n.getString("coder.title", project));
            jbTabs.addTab(encoderTabInfo);

            TabInfo decoderTabInfo = new TabInfo(new ScriptView(project));
            decoderTabInfo.setText(I18n.getString("script.title", project));
            jbTabs.addTab(decoderTabInfo);

            TabInfo logTab = new TabInfo(new LogView(project));
            logTab.setText(I18n.getString("log.title", project));
            jbTabs.addTab(logTab);
            add(jbTabs.getComponent(), BorderLayout.CENTER);
        } catch (Throwable e) {
            JDialog jd = new JDialog();
            jd.setTitle("启动插件失败提示");
            jd.setSize(600,400);
            jd.setAlwaysOnTop(true);
            JBTextArea jbTextArea = new JBTextArea();
            jbTextArea.setEditable(false);
            jbTextArea.setText(e.getMessage() + "\n");
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                jbTextArea.append(stackTraceElement.toString() + "\n");
            }
            JBScrollPane jbScrollPane = new JBScrollPane(jbTextArea);
            jd.getContentPane().add(jbScrollPane);
            jd.setLocationRelativeTo(null);
            jd.setVisible(true);
        }
        return this;
    }

    @Override
    public void showTool() {

    }

    @Override
    public void closeTool() {

    }
}
