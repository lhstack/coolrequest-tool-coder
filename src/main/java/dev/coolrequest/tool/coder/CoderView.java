package dev.coolrequest.tool.coder;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CoderView extends JPanel implements DocumentListener {
    private final JComboBox<String> encoderSourceBox = new JComboBox<>();
    private final JComboBox<String> encoderTargetBox = new JComboBox<>();
    private final LeftSource leftSource = new LeftSource();
    private final RightTarget rightTarget = new RightTarget();
    private final List<Coder> coders;

    public CoderView() {
        super(new BorderLayout());
        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setSecondComponent(rightTarget);
        jbSplitter.setFirstComponent(leftSource);
        //加载Coder的实现
        List<Class<?>> encoderClasses = ClassLoaderUtils.scan(clazz -> true, "dev.coolrequest.tool.coder.impl");
        this.coders = encoderClasses.stream().map(item -> {
                    try {
                        return item.getConstructor().newInstance();
                    } catch (Throwable ignore) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .map(Coder.class::cast)
                .sorted(Comparator.comparing(Coder::ordered))
                .collect(Collectors.toList());
        //左侧下拉框内容
        Set<String> source = new HashSet<>();
        //右侧下拉框内容
        Set<String> target = new HashSet<>();
        //左侧第一个下拉框对应的Coder
        Coder coder = coders.get(0);
        coders.forEach(encoder -> {
            //填充左侧下拉框内容
            source.add(encoder.kind().source);
            //填充右侧下拉框内容,前提是左侧第一个下拉框支持的
            if (StringUtils.equals(encoder.kind().source, coder.kind().source)) {
                target.add(encoder.kind().target);
            }
        });

        //添加到box中
        source.forEach(encoderSourceBox::addItem);
        target.forEach(encoderTargetBox::addItem);

        //添加左侧下拉框数据变更监听器,当左侧下拉框数据发生变更,联动更新右侧下拉框内容
        encoderSourceBox.addItemListener(e -> {
            String sourceValue = String.valueOf(encoderSourceBox.getSelectedItem());
            encoderTargetBox.removeAllItems();
            coders.stream().filter(item -> StringUtils.equals(item.kind().source, sourceValue)).map(item -> item.kind().target).forEach(encoderTargetBox::addItem);
            encoder();
        });
        encoderTargetBox.addItemListener(e -> encoder());

        add(jbSplitter, BorderLayout.CENTER);
    }

    private void encoder() {
        Object encoderValue = encoderSourceBox.getSelectedItem();
        if (encoderValue == null) return;

        Object targetValue = encoderTargetBox.getSelectedItem();
        if (targetValue == null) return;
        if (leftSource.getSourceTextArea().getText().equalsIgnoreCase("")) return;
        for (Coder coder : this.coders) {
            if (coder.kind().is(String.valueOf(encoderValue), String.valueOf(targetValue))) {
                //转换
                rightTarget.getTargetTextArea().setText(coder.transform(leftSource.getSourceTextArea().getText()));
            }
        }
    }

    //创建FlowLayout布局面板,靠左对齐
    private JPanel createFlowLayoutPanel(JComponent component) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.add(component);
        return jPanel;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        encoder();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        encoder();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        encoder();
    }

    private class RightTarget extends JPanel {
        private final JBTextArea targetTextArea = new JBTextArea();

        public RightTarget() {
            super(new BorderLayout());
            targetTextArea.setEditable(false);
            //添加下拉框,左对齐
            add(createFlowLayoutPanel(encoderTargetBox), BorderLayout.NORTH);
            //内容框
            add(new JScrollPane(targetTextArea), BorderLayout.CENTER);
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
            add(createFlowLayoutPanel(encoderSourceBox), BorderLayout.NORTH);
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
