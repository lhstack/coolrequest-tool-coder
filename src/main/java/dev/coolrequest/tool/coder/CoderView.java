package dev.coolrequest.tool.coder;

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
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.custom.*;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.LogContext;
import dev.coolrequest.tool.common.Logger;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.GlobalStateManager;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
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

    public CoderView(Project project) {
        super(new BorderLayout());

        LogContext logContext = LogContext.getInstance(project);
        this.logger = logContext.getLogger(CoderView.class);
        //加载Coder的实现
        List<Class<?>> coderClasses = ClassLoaderUtils.scan(clazz -> true, "dev.coolrequest.tool.coder.impl");
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
        leftSource = new LeftSource();
        rightTarget = new RightTarget(project, createGroovyShell(project));
        GlobalState globalState = GlobalStateManager.loadState(project);
        Object customCoderScript = globalState.getCache("CustomCoderScript");
        if (customCoderScript != null) {
            String script = String.valueOf(customCoderScript);
            logger.info("load custom coders");
            loadCustomCoders(script, createGroovyShell(project));
        } else {
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
        StringBuilder logBuffer = new StringBuilder();
        Consumer<Object> log = s -> {
            logBuffer.append(s).append("\n");
        };
        CoderRegistry coderRegistry = new CoderRegistry(dynamicCoders);
        Binding binding = new Binding();
        binding.setVariable("coder", coderRegistry);
        binding.setVariable("log", log);
        Script script = groovyShell.get().parse(customCoderScript);
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
            this.logger.info("load coders: " + dynamicCoders);
            this.logger.info("coder sources: " + source);
            this.logger.info("target sources: " + target);
        }
        List<Coder> noRegistryCoders = coderRegistry.getNoRegistryCoders();
        if (CollectionUtils.isNotEmpty(noRegistryCoders)) {
            String noRegistryCodersLog = noRegistryCoders.stream().map(item -> String.format("source: %s, target: %s", item.kind().source, item.kind().target)).collect(Collectors.joining("\n"));
            log.accept("以上coder已经存在,不能注册: \n" + noRegistryCodersLog);
        }
        if (logBuffer.length() > 0) {
            rightTextField.setText(logBuffer.toString());
        }
    }

    /**
     * 创建groovyShell
     *
     * @return
     */
    private Supplier<GroovyShell> createGroovyShell(Project project) {

        return () -> {
            GroovyShell groovyShell = new GroovyShell();
            if (GlobalStateManager.loadState(project).isCustomCoderUsingProjectLibrary()) {
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

    //创建FlowLayout布局面板,靠左对齐
    private JPanel createFlowLayoutPanel(JComponent component, int layout) {
        JPanel jPanel = new JPanel(new FlowLayout(layout));
        jPanel.add(component);
        return jPanel;
    }

    private JPanel createJustifiedPanel(JComponent left, JComponent right) {
        JPanel jPanel = new JPanel(new GridLayout(1, 2));
        jPanel.add(createFlowLayoutPanel(left, FlowLayout.LEFT));
        jPanel.add(createFlowLayoutPanel(right, FlowLayout.RIGHT));
        return jPanel;
    }

    @Override
    public void documentChanged(com.intellij.openapi.editor.event.@NotNull DocumentEvent event) {
        transform();
    }

    private class RightTarget extends JPanel {
        private final Project project;

        private final AtomicBoolean state = new AtomicBoolean(false);

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
            JPanel jPanel = new JPanel(new GridLayout(1, 2));
            jPanel.add(clearButton);
            jPanel.add(customCoder);
            //添加下拉框,左对齐
            add(createJustifiedPanel(coderTargetBox, jPanel), BorderLayout.NORTH);
            //内容框
            add(new JScrollPane(rightTextField), BorderLayout.CENTER);
        }

        /**
         * 自定义Coder点击事件
         */
        private void customCoderMouseClicked(Supplier<GroovyShell> groovyShell) {
            if (state.compareAndSet(false, true)) {
                JDialog dialog = new JDialog();
                dialog.setSize(1000, 600);
                dialog.setAlwaysOnTop(true);
                dialog.setLocationRelativeTo(null);
                dialog.getContentPane().add(createCustomCoderPanel(groovyShell));
                dialog.setVisible(true);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        state.set(false);
                    }
                });
            }
        }

        private Component createCustomCoderPanel(Supplier<GroovyShell> groovyShell) {
            LanguageFileType groovyFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension("groovy");
            MultiLanguageTextField leftFieldText = new MultiLanguageTextField(groovyFileType, project);
            Object customCoderScript = GlobalStateManager.loadState(project).getCache("CustomCoderScript");
            if (customCoderScript != null) {
                String script = String.valueOf(customCoderScript);
                if (StringUtils.isNotBlank(script)) {
                    leftFieldText.setText(script);
                }
            }

            JBTextArea rightFieldText = new JBTextArea();
            rightFieldText.setEditable(false);
            //设置actionGroup
            DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
            defaultActionGroup.add(new DemoAction(leftFieldText, rightFieldText, project));
            defaultActionGroup.add(new CompileAction(leftFieldText, rightFieldText, groovyShell, project));
            defaultActionGroup.add(new InstallAction(leftFieldText, rightFieldText, groovyShell, coderSourceBox, coderTargetBox, baseCoders, dynamicCoders, project));
            defaultActionGroup.add(new UsingProjectLibraryAction(project));
            SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, false);
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("custom.coder", defaultActionGroup, false);
            panel.setToolbar(actionToolbar.getComponent());
            JBSplitter splitter = new JBSplitter();
            splitter.setFirstComponent(leftFieldText);
            splitter.setSecondComponent(rightFieldText);
            //设置内容
            panel.setContent(splitter);
            return panel;
        }

    }

    private class LeftSource extends JPanel {

        public LeftSource() {
            super(new BorderLayout());
            //下拉框
            add(createFlowLayoutPanel(coderSourceBox, FlowLayout.LEFT), BorderLayout.NORTH);
            //内容框
            add(leftTextField, BorderLayout.CENTER);
        }

    }
}
