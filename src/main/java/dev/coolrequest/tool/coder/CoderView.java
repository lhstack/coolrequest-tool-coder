package dev.coolrequest.tool.coder;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.custom.CompileAction;
import dev.coolrequest.tool.coder.custom.DemoAction;
import dev.coolrequest.tool.coder.custom.InstallAction;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CoderView extends JPanel implements DocumentListener {
    private final JComboBox<String> coderSourceBox = new JComboBox<>();
    private final JComboBox<String> coderTargetBox = new JComboBox<>();
    private final LeftSource leftSource;
    private final RightTarget rightTarget;
    private final List<Coder> baseCoders;
    private final List<Coder> dynamicCoders;

    public CoderView(Project project) {
        super(new BorderLayout());

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
        GroovyShell groovyShell = createGroovyShell();
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

        //添加左侧下拉框数据变更监听器,当左侧下拉框数据发生变更,联动更新右侧下拉框内容
        coderSourceBox.addItemListener(e -> {
            String sourceValue = String.valueOf(coderSourceBox.getSelectedItem());
            coderTargetBox.removeAllItems();
            dynamicCoders.stream().filter(item -> StringUtils.equals(item.kind().source, sourceValue)).map(item -> item.kind().target).forEach(coderTargetBox::addItem);
            transform();
        });
        coderTargetBox.addItemListener(e -> transform());

        rightTarget = new RightTarget(project,groovyShell);
        this.leftSource = new LeftSource();

        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setFirstComponent(leftSource);
        jbSplitter.setSecondComponent(rightTarget);

        add(jbSplitter, BorderLayout.CENTER);
    }

    /**
     * 创建groovyShell
     * @return
     */
    private GroovyShell createGroovyShell() {
        return new GroovyShell();
    }

    private void transform() {
        Object sourceCoderValue = coderSourceBox.getSelectedItem();
        if (sourceCoderValue == null) return;
        Object targetValue = coderTargetBox.getSelectedItem();
        if (targetValue == null) return;
        if (leftSource.getSourceTextArea().getText().equalsIgnoreCase("")) return;
        for (Coder coder : this.dynamicCoders) {
            if (coder.kind().is(String.valueOf(sourceCoderValue), String.valueOf(targetValue))) {
                //转换
                rightTarget.getTargetTextArea().setText(coder.transform(leftSource.getSourceTextArea().getText()));
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
    public void insertUpdate(DocumentEvent e) {
        transform();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        transform();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        transform();
    }

    private class RightTarget extends JPanel {
        private final JBTextArea targetTextArea = new JBTextArea();
        private final Project project;

        private final AtomicBoolean state = new AtomicBoolean(false);

        public RightTarget(Project project, GroovyShell groovyShell) {
            super(new BorderLayout());
            this.project = project;
            targetTextArea.setEditable(false);
            JButton customCoder = new JButton(I18n.getString("coder.custom.title"));
            customCoder.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            customCoderMouseClicked(groovyShell);
                        } catch (Throwable err) {
                            Messages.showErrorDialog(err.getMessage(), I18n.getString("coder.custom.title"));
                        }
                    }
                }
            });
            //添加下拉框,左对齐
            add(createJustifiedPanel(coderTargetBox, customCoder), BorderLayout.NORTH);
            //内容框
            add(new JScrollPane(targetTextArea), BorderLayout.CENTER);
        }

        /**
         * 自定义Coder点击事件
         */
        private void customCoderMouseClicked(GroovyShell groovyShell) {
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

        private Component createCustomCoderPanel(GroovyShell groovyShell) {
            LanguageFileType groovyFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension("groovy");
            MultiLanguageTextField leftFieldText = new MultiLanguageTextField(groovyFileType, project);
            JBTextArea rightFieldText = new JBTextArea();
            rightFieldText.setEditable(false);
            //设置actionGroup
            DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
            defaultActionGroup.add(new DemoAction(leftFieldText, rightFieldText));
            defaultActionGroup.add(new CompileAction(leftFieldText, rightFieldText,groovyShell));
            defaultActionGroup.add(new InstallAction(leftFieldText, rightFieldText,groovyShell,coderSourceBox,coderTargetBox,baseCoders,dynamicCoders));
            SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, false);
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("custom.coder", defaultActionGroup, false);
            actionToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
            panel.setToolbar(actionToolbar.getComponent());
            JBSplitter splitter = new JBSplitter();
            splitter.setFirstComponent(leftFieldText);
            splitter.setSecondComponent(rightFieldText);
            //设置内容
            panel.setContent(splitter);
            return panel;
        }

        public JBTextArea getTargetTextArea() {
            return targetTextArea;
        }
    }

    private class LeftSource extends JPanel {
        private final JBTextArea sourceTextArea = new JBTextArea();

        public LeftSource() {
            super(new BorderLayout());
            //下拉框
            add(createFlowLayoutPanel(coderSourceBox, FlowLayout.LEFT), BorderLayout.NORTH);
            //内容框
            add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
            //监听内容变更
            sourceTextArea.getDocument().addDocumentListener(CoderView.this);
        }

        public JBTextArea getSourceTextArea() {
            return sourceTextArea;
        }
    }
}
