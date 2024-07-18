package dev.coolrequest.tool.views.log;

import com.intellij.openapi.project.Project;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.LogContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LogView extends JPanel {
    private final Project project;

    public LogView(Project project) {
        super(new BorderLayout());
        this.project = project;
        LogContext logContext = LogContext.getInstance(project);
        JButton clearLogButton = new JButton(I18n.getString("script.clearLog", project));
        clearLogButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    logContext.getTextArea().setText("");
                }
            }
        });
        JPanel clearLogButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clearLogButtonPanel.add(clearLogButton);
        //添加下拉框,左对齐
        add(clearLogButtonPanel, BorderLayout.NORTH);
        //内容框
        add(logContext.getLogComponent(), BorderLayout.CENTER);
    }
}
