package dev.coolrequest.tool.common;

import com.intellij.ui.components.JBTextArea;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextAreaLogger implements Logger {

    private final JBTextArea textArea;

    private final String loggerName;

    public TextAreaLogger(Class<?> clazz, JBTextArea textArea) {
        this(clazz.getName(), textArea);
    }

    public TextAreaLogger(String loggerName, JBTextArea textArea) {
        this.textArea = textArea;
        this.loggerName = loggerName;
    }

    @Override
    public void info(Object msg) {
        this.message("INFO", msg);
    }

    @Override
    public void warn(Object msg) {
        this.message("WARN", msg);
    }

    private void message(String level, Object msg) {
        String text = textArea.getText();
        String message = msg != null ? msg.toString() : "";
        String log = String.format("%s [%s] %s #%s - %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), Thread.currentThread().getName(), level, loggerName, message);
        if (StringUtils.isNotBlank(text)) {
            textArea.append("\n");
            textArea.append(log);
        } else {
            textArea.append(log);
        }
    }

    @Override
    public void error(Object msg) {
        this.message("ERROR", msg);
    }
}
