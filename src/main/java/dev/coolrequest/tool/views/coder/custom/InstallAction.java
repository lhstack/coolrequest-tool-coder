package dev.coolrequest.tool.views.coder.custom;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.*;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.ProjectState;
import dev.coolrequest.tool.state.ProjectStateManager;
import dev.coolrequest.tool.state.Scope;
import dev.coolrequest.tool.views.coder.Coder;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InstallAction extends AnAction {

    private final MultiLanguageTextField codeTextField;
    private final JBTextArea outputTextField;
    private final Supplier<GroovyShell> groovyShell;
    private final JComboBox<String> coderSourceBox;
    private final List<Coder> baseCoders;
    private final List<Coder> dynamicCoders;
    private final Project project;
    private final Logger logger;
    private final ProjectState projectState;

    public InstallAction(MultiLanguageTextField codeTextField, JBTextArea outputTextField, Supplier<GroovyShell> groovyShell, JComboBox<String> coderSourceBox, List<Coder> baseCoders, List<Coder> dynamicCoders, Project project) {
        super(() -> I18n.getString("coder.custom.install", project), Icons.INSTALL);
        this.codeTextField = codeTextField;
        this.outputTextField = outputTextField;
        this.groovyShell = groovyShell;
        this.coderSourceBox = coderSourceBox;
        this.baseCoders = baseCoders;
        this.dynamicCoders = dynamicCoders;
        this.project = project;
        this.projectState = ProjectStateManager.load(project);
        this.logger = LogContext.getInstance(project).getLogger(InstallAction.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        String code = this.codeTextField.getText();
        if (StringUtils.isNotBlank(code)) {
            TextAreaLogger contextLogger = new TextAreaLogger("custom.coder", outputTextField);
            this.dynamicCoders.clear();
            this.dynamicCoders.addAll(this.baseCoders);
            CoderRegistry coderRegistry = new CoderRegistry(this.dynamicCoders);
            Binding binding = new Binding();
            binding.setVariable("coder", coderRegistry);
            binding.setVariable("log", contextLogger);
            binding.setVariable("sysLog", this.logger);
            binding.setVariable("projectEnv", ProjectStateManager.load(project).getJsonObjCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT));
            binding.setVariable("globalEnv", GlobalState.getJsonObjCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT));
            Script script = this.groovyShell.get().parse(code);
            script.setBinding(binding);
            FutureTask<Object> futureTask = new FutureTask<>(script::run);
            try {
                Thread thread = new Thread(futureTask);
                thread.start();
                futureTask.get(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                futureTask.cancel(true);
                contextLogger.error("安装自定义coder失败,错误信息: " + e);
            }
            if (CollectionUtils.isNotEmpty(coderRegistry.getRegistryCoders())) {
                dynamicCoders.clear();
                dynamicCoders.addAll(this.baseCoders);
                dynamicCoders.addAll(coderRegistry.getRegistryCoders());
                dynamicCoders.sort(Comparator.comparing(Coder::ordered));
                //左侧下拉框内容
                Set<String> source = new LinkedHashSet<>();
                dynamicCoders.forEach(coderItem -> {
                    //填充左侧下拉框内容
                    source.add(coderItem.kind().source);
                });
                coderSourceBox.removeAllItems();
                //添加到box中
                source.forEach(coderSourceBox::addItem);
                if (this.projectState.getScope() == Scope.PROJECT) {
                    projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE, codeTextField.getText());
                    ProjectStateManager.store(project);
                } else if (this.projectState.getScope() == Scope.GLOBAL) {
                    GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE, codeTextField.getText());
                }
                contextLogger.info("");
                contextLogger.info("安装成功的 coders: " + coderRegistry.getRegistryCoders());
            }
            List<Coder> noRegistryCoders = coderRegistry.getNoRegistryCoders();
            if (CollectionUtils.isNotEmpty(noRegistryCoders)) {
                String noRegistryCodersLog = noRegistryCoders.stream().map(item -> String.format("source: %s, target: %s", item.kind().source, item.kind().target)).collect(Collectors.joining("\n"));
                contextLogger.info("以上coder已经存在,不能注册: \n" + noRegistryCodersLog);
                contextLogger.info("未安装成功的 coders: " + noRegistryCoders);
            }
        }
    }
}
