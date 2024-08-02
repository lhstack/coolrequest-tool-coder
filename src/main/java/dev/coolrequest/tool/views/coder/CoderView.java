package dev.coolrequest.tool.views.coder;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.*;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.components.PopupMenu;
import dev.coolrequest.tool.components.SimpleFrame;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.ProjectState;
import dev.coolrequest.tool.state.ProjectStateManager;
import dev.coolrequest.tool.state.Scope;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
import dev.coolrequest.tool.utils.ComponentUtils;
import dev.coolrequest.tool.views.coder.custom.*;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CoderView extends JPanel implements DocumentListener {

    private final JComboBox<String> coderSourceBox = new JComboBox<>();
    private final JComboBox<String> coderTargetBox = new JComboBox<>();
    private final LeftSource leftSource;
    private final RightTarget rightTarget;
    private final List<Coder> baseCoders;
    private final List<Coder> dynamicCoders;
    private final Logger logger;
    private final MultiLanguageTextField leftTextField;
    private final MultiLanguageTextField rightTextField;
    private final Project project;

    public CoderView(Project project) {
        super(new BorderLayout());
        this.project = project;
        LogContext logContext = LogContext.getInstance(project);
        this.logger = logContext.getLogger(CoderView.class);
        //加载Coder的实现
        List<Class<?>> coderClasses = ClassLoaderUtils.scan(clazz -> true, "dev.coolrequest.tool.views.coder.impl");
        this.baseCoders = coderClasses.stream().map(item -> {
                    try {
                        return item.getConstructor().newInstance();
                    } catch (Throwable ignore) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .map(Coder.class::cast)
                .sorted(Comparator.comparing(Coder::ordered))
                .collect(Collectors.toList());
        dynamicCoders = new ArrayList<>(baseCoders);

        this.leftTextField = new MultiLanguageTextField(PlainTextFileType.INSTANCE, project);
        this.leftTextField.getDocument().addDocumentListener(this);
        this.rightTextField = new MultiLanguageTextField(PlainTextFileType.INSTANCE, project);
        this.rightTextField.setEnabled(false);
        leftSource = new LeftSource();
        rightTarget = new RightTarget(project, createGroovyShell(project));
        ProjectState projectState = ProjectStateManager.load(project);
        String customCoderScript = projectState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse(null);
        String globalCustomCoderScript = GlobalState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse(null);
        if (customCoderScript != null || globalCustomCoderScript != null) {
            logger.info("load custom coders");
            if (customCoderScript != null) {
                loadCustomCoders(customCoderScript, createGroovyShell(project));
            }
            if (globalCustomCoderScript != null) {
                loadCustomCoders(globalCustomCoderScript, createGroovyShell(project));
            }
        } else {
            //左侧下拉框内容
            Set<String> source = new LinkedHashSet<>();
            //右侧下拉框内容
            Set<String> target = new HashSet<>();
            dynamicCoders.sort(Comparator.comparing(Coder::ordered));
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

            //添加到box中
            source.forEach(coderSourceBox::addItem);
            target.forEach(coderTargetBox::addItem);
        }
        //添加左侧下拉框数据变更监听器,当左侧下拉框数据发生变更,联动更新右侧下拉框内容
        coderSourceBox.addItemListener(e -> {
            String sourceValue = String.valueOf(coderSourceBox.getSelectedItem());
            coderTargetBox.removeAllItems();
            dynamicCoders.stream().filter(item -> StringUtils.equals(item.kind().source, sourceValue)).map(item -> item.kind().target).forEach(coderTargetBox::addItem);
            transform();
        });
        coderTargetBox.addItemListener(e -> transform());
        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setFirstComponent(leftSource);
        jbSplitter.setSecondComponent(rightTarget);

        add(jbSplitter, BorderLayout.CENTER);
    }

    /**
     * 加载自定义coder
     *
     * @param customCoderScript
     * @param groovyShell
     */
    private void loadCustomCoders(String customCoderScript, Supplier<GroovyShell> groovyShell) {
        CoderRegistry coderRegistry = new CoderRegistry(dynamicCoders);
        Binding binding = new Binding();
        binding.setVariable("coder", coderRegistry);
        binding.setVariable("sysLog", logger);
        binding.setVariable("log", logger);
        binding.setVariable("projectEnv", ProjectStateManager.load(this.project).getJsonObjCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT));
        binding.setVariable("globalEnv", GlobalState.getJsonObjCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT));
        Script script = groovyShell.get().parse(customCoderScript);
        script.setBinding(binding);
        FutureTask<Object> futureTask = new FutureTask<>(script::run);
        try {
            Thread thread = new Thread(futureTask);
            thread.start();
            futureTask.get(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
            futureTask.cancel(true);
            logger.error("安装自定义coder失败,错误信息: " + e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(coderRegistry.getRegistryCoders())) {
            dynamicCoders.clear();
            dynamicCoders.addAll(this.baseCoders);
            dynamicCoders.addAll(coderRegistry.getRegistryCoders());
            dynamicCoders.sort(Comparator.comparing(Coder::ordered));
            //左侧下拉框内容
            Set<String> source = new LinkedHashSet<>();
            //右侧下拉框内容
            Set<String> target = new LinkedHashSet<>();
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
            this.logger.info("load coders: " + dynamicCoders);
            this.logger.info("coder sources: " + source);
            this.logger.info("target sources: " + target);
        }
        List<Coder> noRegistryCoders = coderRegistry.getNoRegistryCoders();
        if (CollectionUtils.isNotEmpty(noRegistryCoders)) {
            String noRegistryCodersLog = noRegistryCoders.stream().map(item -> String.format("source: %s, target: %s", item.kind().source, item.kind().target)).collect(Collectors.joining("\n"));
            logger.info("以上coder已经存在,不能注册: \n" + noRegistryCodersLog);
        }
    }

    /**
     * 创建groovyShell
     *
     * @return
     */
    private Supplier<GroovyShell> createGroovyShell(Project project) {

        return () -> {
            GroovyShell groovyShell = new GroovyShell(CoderView.class.getClassLoader());
            if (ProjectStateManager.load(project).isCustomCoderUsingProjectLibrary()) {
                try {
                    for (Library library : LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries()) {
                        for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
                            URL url = new File(file.getPresentableUrl()).toURI().toURL();
                            groovyShell.getClassLoader().addURL(url);
                        }
                    }
                } catch (Throwable e) {
                    logger.error("create groovyShell 失败,错误信息: " + e.getMessage());
                }
            }
            return groovyShell;
        };
    }

    private void transform() {
        Object sourceCoderValue = coderSourceBox.getSelectedItem();
        if (sourceCoderValue == null) return;
        Object targetValue = coderTargetBox.getSelectedItem();
        if (targetValue == null) return;
        if (this.leftTextField.getText().equalsIgnoreCase("")) return;
        for (Coder coder : this.dynamicCoders) {
            if (coder.kind().is(String.valueOf(sourceCoderValue), String.valueOf(targetValue))) {
                //转换
                rightTextField.setText(coder.transform(this.leftTextField.getText()));
            }
        }
    }


    @Override
    public void documentChanged(com.intellij.openapi.editor.event.@NotNull DocumentEvent event) {
        transform();
    }

    private class RightTarget extends JPanel {
        private final Project project;

        private final AtomicBoolean state = new AtomicBoolean(false);
        private SimpleFrame coder;

        public RightTarget(Project project, Supplier<GroovyShell> groovyShell) {
            super(new BorderLayout());
            this.project = project;
            JButton customCoder = new JButton(I18n.getString("coder.custom.title", project));
            customCoder.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            customCoderMouseClicked(groovyShell);
                        } catch (Throwable err) {
                            Messages.showErrorDialog(err.getMessage(), I18n.getString("coder.custom.title", project));
                        }
                    }
                }
            });
            JButton clearButton = new JButton(I18n.getString("coder.editor.clear", project));
            clearButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (StringUtils.isBlank(leftTextField.getText()) && StringUtils.isBlank(rightTextField.getText())) {
                            return;
                        }
                        logger.warn("清空编辑器中的内容中...");
                        logger.warn("left: \n" + leftTextField.getText());
                        logger.warn("right: \n" + rightTextField.getText());
                        leftTextField.setText("");
                        rightTextField.setText("");
                        logger.warn("清空编辑器中的内容完毕");
                    }
                }
            });
            JPanel jPanel = new JPanel(new GridLayout(1, 3));
            jPanel.add(coderTargetBox);
            jPanel.add(clearButton);
            jPanel.add(customCoder);
            //添加下拉框,左对齐
            add(ComponentUtils.createFlowLayoutPanel(jPanel, FlowLayout.LEFT), BorderLayout.NORTH);
            //内容框
            add(new JScrollPane(rightTextField), BorderLayout.CENTER);
        }

        /**
         * 自定义Coder点击事件
         */
        private void customCoderMouseClicked(Supplier<GroovyShell> groovyShell) {
            if (state.compareAndSet(false, true)) {
                List<Runnable> disposes = new ArrayList<>();
                this.coder = new SimpleFrame(createCustomCoderPanel(groovyShell, disposes), I18n.getString("coder.custom.title", project), new Dimension(1000, 600));
                coder.setVisible(true);
                coder.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        disposes.forEach(Runnable::run);
                        state.set(false);
                    }
                });
            } else {
                this.coder.toFront();
            }
        }

        private JComponent createCustomCoderPanel(Supplier<GroovyShell> groovyShell, List<Runnable> disposeRegistry) {
            LanguageFileType groovyFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension("groovy");
            MultiLanguageTextField leftFieldText = new MultiLanguageTextField(groovyFileType, project);
            ProjectState projectState = ProjectStateManager.load(project);
            if (projectState.getScope() == Scope.PROJECT) {
                String script = projectState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse("");
                leftFieldText.setText(script);
            } else {
                String script = GlobalState.getOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_SCRIPT_CODE).orElse("");
                leftFieldText.setText(script);
            }
            JBTextArea rightFieldText = new JBTextArea();
            rightFieldText.setEditable(false);
            //设置actionGroup
            DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
            defaultActionGroup.add(new EnvAction(project, disposeRegistry));
            defaultActionGroup.add(new DemoAction(leftFieldText, rightFieldText, project));
            defaultActionGroup.add(new CompileAction(leftFieldText, rightFieldText, groovyShell, project));
            defaultActionGroup.add(new InstallAction(leftFieldText, rightFieldText, groovyShell, coderSourceBox, baseCoders, dynamicCoders, project));
            defaultActionGroup.add(new UsingProjectLibraryAction(project));
            defaultActionGroup.add(new RunAction(leftFieldText, rightFieldText, groovyShell, project));
            defaultActionGroup.add(new ChangeScopeAction(leftFieldText, project));
            SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, false);
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("custom.coder", defaultActionGroup, false);
            panel.setToolbar(actionToolbar.getComponent());
            JBSplitter splitter = new JBSplitter();
            splitter.setFirstComponent(leftFieldText);
            JBScrollPane jbScrollPane = new JBScrollPane(rightFieldText);
            splitter.setSecondComponent(jbScrollPane);
            //设置内容
            panel.setContent(splitter);
            PopupMenu.attachClearMenu(I18n.getString("script.clearLog", project), Icons.CLEAR, rightFieldText);
            return panel;
        }

    }

    private class LeftSource extends JPanel {

        public LeftSource() {
            super(new BorderLayout());
            //下拉框
            add(ComponentUtils.createFlowLayoutPanel(coderSourceBox, FlowLayout.LEFT), BorderLayout.NORTH);
            //内容框
            add(leftTextField, BorderLayout.CENTER);
        }

    }
}
