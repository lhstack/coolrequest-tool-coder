package dev.coolrequest.tool.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import org.jetbrains.annotations.NotNull;

public class SaveAction extends AnAction {
    public SaveAction(MultiLanguageTextField leftFieldText, JBTextArea rightFieldText) {
        super(() -> I18n.getString("coder.custom.save"), Icons.SAVE_ACTION);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

    }
}
