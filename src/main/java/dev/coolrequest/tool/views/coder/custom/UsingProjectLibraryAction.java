package dev.coolrequest.tool.views.coder.custom;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.GlobalStateManager;
import org.jetbrains.annotations.NotNull;

/**
 * 安装项目依赖
 */
public class UsingProjectLibraryAction extends ToggleAction {


    private final Project project;

    public UsingProjectLibraryAction(Project project) {
        super(() -> I18n.getString("coder.custom.usingProjectLibrary", project), Icons.LIBRARY);
        this.project = project;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return GlobalStateManager.loadState(project).isCustomCoderUsingProjectLibrary();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        GlobalState globalState = GlobalStateManager.loadState(project);
        if (globalState.isCustomCoderUsingProjectLibrary() != state) {
            globalState.setCustomCoderUsingProjectLibrary(state);
            GlobalStateManager.persistence(project);
        }
    }
}
