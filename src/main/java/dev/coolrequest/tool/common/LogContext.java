package dev.coolrequest.tool.common;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        return new Logger() {
            @Override
            public void info(String msg) {
                this.message("INFO", msg);
            }

            @Override
            public void warn(String msg) {
                this.message("WARN", msg);
            }

            private void message(String level, String msg) {
                String text = textArea.getText();
                String log = String.format("%s [%s] %s #%s - %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Thread.currentThread().getName(), level, clazz.getName(), msg);
                if (StringUtils.isNotBlank(text)) {
                    textArea.append("\n");
                    textArea.append(log);
                } else {
                    textArea.append(log);
                }
            }

            @Override
            public void error(String msg) {
                this.message("ERROR", msg);
            }
        };
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
