package dev.coolrequest.tool.common;

import com.intellij.openapi.project.Project;
import dev.coolrequest.tool.state.ProjectStateManager;

import java.util.ResourceBundle;

public class I18n {

    public static String getString(String key, Project project) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/msg", ProjectStateManager.load(project).getLocale());
        return bundle.getString(key);
    }
}
