package dev.coolrequest.tool.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.common.LogContext;
import dev.coolrequest.tool.common.Logger;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.GlobalStateManager;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InstallAction extends AnAction {

    private final MultiLanguageTextField codeTextField;
    private final JBTextArea outputTextField;
    private final Supplier<GroovyShell> groovyShell;
    private final JComboBox<String> coderSourceBox;
    private final JComboBox<String> coderTargetBox;
    private final List<Coder> baseCoders;
    private final List<Coder> dynamicCoders;
    private final Project project;
    private final Logger logger;

    public InstallAction(MultiLanguageTextField codeTextField, JBTextArea outputTextField, Supplier<GroovyShell> groovyShell, JComboBox<String> coderSourceBox, JComboBox<String> coderTargetBox, List<Coder> baseCoders, List<Coder> dynamicCoders, Project project) {
        super(() -> I18n.getString("coder.custom.install", project), Icons.INSTALL_ACTION);
        this.codeTextField = codeTextField;
        this.outputTextField = outputTextField;
        this.groovyShell = groovyShell;
        this.coderSourceBox = coderSourceBox;
        this.coderTargetBox = coderTargetBox;
        this.baseCoders = baseCoders;
        this.dynamicCoders = dynamicCoders;
        this.project = project;
        this.logger = LogContext.getInstance(project).getLogger(InstallAction.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        String code = this.codeTextField.getText();
        if (StringUtils.isNotBlank(code)) {
            StringBuilder logBuffer = new StringBuilder();
            Consumer<Object> log = s -> {
                logBuffer.append(s).append("\n");
            };
            CoderRegistry coderRegistry = new CoderRegistry(this.dynamicCoders);
            Binding binding = new Binding();
            binding.setVariable("coder", coderRegistry);
            binding.setVariable("log", log);
            Script script = this.groovyShell.get().parse(code);
            script.setBinding(binding);
            script.run();
            if (CollectionUtils.isNotEmpty(coderRegistry.getRegistryCoders())) {
                dynamicCoders.clear();
                dynamicCoders.addAll(this.baseCoders);
                dynamicCoders.addAll(coderRegistry.getRegistryCoders());
                //左侧下拉框内容
                Set<String> source = new HashSet<>();
                //右侧下拉框内容
                Set<String> target = new HashSet<>();
                //左侧第一个下拉框对应的Coder
                Coder coder = dynamicCoders.get(0);
                dynamicCoders.forEach(coderItem -> {
                    //填充左侧下拉框内容
                    source.add(coderItem.kind().source);
                    //填充右侧下拉框内容,前提是左侧第一个下拉框支持的
                    if (StringUtils.equals(coderItem.kind().source, coder.kind().source)) {
                        target.add(coderItem.kind().target);
                    }
                });
                coderSourceBox.removeAllItems();
                coderTargetBox.removeAllItems();
                //添加到box中
                source.forEach(coderSourceBox::addItem);
                target.forEach(coderTargetBox::addItem);
                GlobalState globalState = GlobalStateManager.loadState(project);
                globalState.putCache("CustomCoderScript", codeTextField.getText());
                GlobalStateManager.persistence(project);
                this.logger.info("");
                this.logger.info("install coders: " + coderRegistry.getRegistryCoders());
                this.logger.info("coder sources: " + source);
                this.logger.info("target sources: " + target);
            }
            List<Coder> noRegistryCoders = coderRegistry.getNoRegistryCoders();
            if (CollectionUtils.isNotEmpty(noRegistryCoders)) {
                String noRegistryCodersLog = noRegistryCoders.stream().map(item -> String.format("source: %s, target: %s", item.kind().source, item.kind().target)).collect(Collectors.joining("\n"));
                log.accept("以上coder已经存在,不能注册: \n" + noRegistryCodersLog);
                logger.info("no install coders: " + noRegistryCoders);
            }
            if (logBuffer.length() > 0) {
                outputTextField.setText(logBuffer.toString());
            }

        }
    }
}
