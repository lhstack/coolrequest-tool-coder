package dev.coolrequest.tool.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class CompileAction extends AnAction {

    private final MultiLanguageTextField codeTextField;
    private final JBTextArea outputTextArea;
    private final GroovyShell groovyShell;

    public CompileAction(MultiLanguageTextField codeTextField, JBTextArea outputTextArea, GroovyShell groovyShell) {
        super(() -> I18n.getString("coder.custom.compile"), Icons.COMPILE_ACTION);
        this.codeTextField = codeTextField;
        this.outputTextArea = outputTextArea;
        this.groovyShell = groovyShell;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (StringUtils.isNotBlank(this.codeTextField.getText())) {
            try {
                this.outputTextArea.setText("开始编译代码...");
                groovyShell.parse(this.codeTextField.getText());
                this.outputTextArea.setText(this.outputTextArea.getText() + "\n" + "编译代码通过");
            } catch (Throwable e) {
                this.outputTextArea.setText(String.format("编译失败,error: %s", e.getMessage()));
            }
        }
    }
}
