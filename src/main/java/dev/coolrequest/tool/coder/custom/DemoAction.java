package dev.coolrequest.tool.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class DemoAction extends AnAction {

    private static final String codeTemplate = new String(ClassLoaderUtils.getResourceToBytes("template/CustomCoder.groovy"), StandardCharsets.UTF_8);
    private final MultiLanguageTextField codeFieldText;

    public DemoAction(MultiLanguageTextField codeFieldText, JBTextArea rightFieldText) {
        super(() -> I18n.getString("coder.custom.demo"), Icons.DEMO_ACTION);
        this.codeFieldText = codeFieldText;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if(StringUtils.isNotBlank(this.codeFieldText.getText()) && !StringUtils.equals(this.codeFieldText.getText(),codeTemplate)){
            int i = JOptionPane.showConfirmDialog(null, "当前编辑器中存在代码,点击确定会覆盖编辑器中存在的代码,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
            if(i == JOptionPane.OK_OPTION){
                this.codeFieldText.setText(codeTemplate);
            }
        }else {
            this.codeFieldText.setText(codeTemplate);
        }

    }
}
