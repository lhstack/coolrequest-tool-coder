package dev.coolrequest.tool.views.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.common.LogContext;
import dev.coolrequest.tool.common.Logger;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CompileAction extends AnAction {

    private final MultiLanguageTextField codeTextField;
    private final JBTextArea outputTextArea;
    private final Supplier<GroovyShell> groovyShell;
    private final Logger logger;

    public CompileAction(MultiLanguageTextField codeTextField, JBTextArea outputTextArea, Supplier<GroovyShell> groovyShell, Project project) {
        super(() -> I18n.getString("coder.custom.compile", project), Icons.COMPILE);
        this.codeTextField = codeTextField;
        this.outputTextArea = outputTextArea;
        this.groovyShell = groovyShell;
        this.logger = LogContext.getInstance(project).getLogger(CompileAction.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (StringUtils.isNotBlank(this.codeTextField.getText())) {
            try {
                groovyShell.get().parse(this.codeTextField.getText());
                this.outputTextArea.setText(this.outputTextArea.getText() + "\n" + "编译代码通过");
            } catch (Throwable e) {
                this.outputTextArea.setText(String.format("编译失败,error: %s", e.getMessage()));
            }
        }
    }
}
