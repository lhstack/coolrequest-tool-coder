package dev.coolrequest.tool.coder.view;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.utils.ClassLoaderUtils;
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
        Set<String> source = new HashSet<>();
        Set<String> target = new HashSet<>();
        Coder coder = coders.get(0);
        coders.forEach(encoder -> {
            source.add(encoder.kind().source);
            if (StringUtils.equals(encoder.kind().source, coder.kind().source)) {
                target.add(encoder.kind().target);
            }
        });

        source.forEach(encoderSourceBox::addItem);
        target.forEach(encoderTargetBox::addItem);

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
                rightTarget.getTargetTextArea().setText(coder.transform(leftSource.getSourceTextArea().getText()));
            }
        }
    }

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
            add(createFlowLayoutPanel(encoderTargetBox), BorderLayout.NORTH);
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
            add(createFlowLayoutPanel(encoderSourceBox), BorderLayout.NORTH);
            add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
            sourceTextArea.getDocument().addDocumentListener(CoderView.this);
        }

        public JBTextArea getSourceTextArea() {
            return sourceTextArea;
        }
    }
}
