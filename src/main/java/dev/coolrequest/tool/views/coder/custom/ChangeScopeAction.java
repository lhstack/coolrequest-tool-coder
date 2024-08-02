package dev.coolrequest.tool.views.coder.custom;

import com.intellij.designer.actions.AbstractComboBoxAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import dev.coolrequest.tool.common.CacheConstant;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.ProjectState;
import dev.coolrequest.tool.state.ProjectStateManager;
import dev.coolrequest.tool.state.Scope;

import java.util.Arrays;

public class ChangeScopeAction extends AbstractComboBoxAction<Scope> {

    private final Project project;

    private final ProjectState projectState;
    private final MultiLanguageTextField leftFieldText;

    public ChangeScopeAction(MultiLanguageTextField leftFieldText, Project project) {
        this.project = project;
        this.projectState = ProjectStateManager.load(project);
        this.leftFieldText = leftFieldText;
        setItems(Arrays.asList(Scope.values()), projectState.getScope());
        setPopupTitle("Scope");
    }

    @Override
    protected void update(Scope scope, Presentation presentation, boolean b) {
        presentation.setText(scope.name().toLowerCase());
    }

    @Override
    protected boolean selectionChanged(Scope scope) {
        if (projectState.getScope() != scope) {
            projectState.setScope(scope);
            if (scope == Scope.PROJECT) {
                leftFieldText.setText(projectState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse(""));
            } else if (scope == Scope.GLOBAL) {
                leftFieldText.setText(GlobalState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse(""));
            }
            ProjectStateManager.store(project);
            return true;
        }
        return false;
    }
}
