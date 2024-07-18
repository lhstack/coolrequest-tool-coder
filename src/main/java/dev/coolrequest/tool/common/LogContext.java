package dev.coolrequest.tool.common;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogContext {

    private static final Map<String, LogContext> CACHE = new ConcurrentHashMap<>();
    private final JBTextArea textArea;

    public LogContext() {
        this.textArea = new JBTextArea();
        this.textArea.setEditable(false);
    }

    public Logger getLogger(Class<?> clazz) {
        return new TextAreaLogger(clazz, textArea);
    }

    public Logger getLogger(String loggerName) {
        return new TextAreaLogger(loggerName, textArea);
    }

    public JComponent getLogComponent() {
        JBScrollPane jbScrollPane = new JBScrollPane(this.textArea);
        jbScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jbScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return jbScrollPane;
    }

    public static LogContext getInstance(Project project) {
        return CACHE.computeIfAbsent(project.getBasePath(), k -> new LogContext());
    }
}
