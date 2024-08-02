package dev.coolrequest.tool.views.coder.custom;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.state.ProjectState;
import dev.coolrequest.tool.state.ProjectStateManager;
import dev.coolrequest.tool.state.Scope;
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
        if (ProjectStateManager.load(project).getScope() == Scope.GLOBAL) {
            return false;
        }
        return ProjectStateManager.load(project).isCustomCoderUsingProjectLibrary();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        if (ProjectStateManager.load(project).getScope() == Scope.GLOBAL) {
            Messages.showWarningDialog("全局作用域下面不可使用项目依赖","警告");
            return ;
        }
        ProjectState projectState = ProjectStateManager.load(project);
        if (projectState.isCustomCoderUsingProjectLibrary() != state) {
            projectState.setCustomCoderUsingProjectLibrary(state);
            ProjectStateManager.store(project);
        }
    }
}
