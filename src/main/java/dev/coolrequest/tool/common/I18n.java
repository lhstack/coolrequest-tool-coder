package dev.coolrequest.tool.common;

import com.intellij.openapi.project.Project;
import dev.coolrequest.tool.state.GlobalStateManager;

import java.util.ResourceBundle;

public class I18n {

    public static String getString(String key, Project project) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/msg", GlobalStateManager.loadState(project).getLocale());
        return bundle.getString(key);
    }
}
