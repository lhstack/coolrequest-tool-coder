package dev.coolrequest.tool.coder;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.encoder.Encoder;
import dev.coolrequest.tool.coder.encoder.Encoders;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class EncoderView extends JPanel implements DocumentListener {
    private final JComboBox<String> encoderSourceBox = new JComboBox<>();
    private final JComboBox<String> encoderTargetBox = new JComboBox<>();
    private final LeftSource leftSource = new LeftSource();
    private final RightTarget rightTarget = new RightTarget();

    public EncoderView() {
        super(new BorderLayout());
        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setSecondComponent(rightTarget);
        jbSplitter.setFirstComponent(leftSource);

        Set<String> source = new HashSet<>();
        Set<String> target = new HashSet<>();
        for (Encoder encoder : Encoders.getEncoders()) {
            source.add(encoder.kind().source);
            target.add(encoder.kind().target);
        }
        source.forEach(encoderSourceBox::addItem);
        target.forEach(encoderTargetBox::addItem);

        encoderSourceBox.addItemListener(e -> encoder());
        encoderTargetBox.addItemListener(e -> encoder());

        add(jbSplitter, BorderLayout.CENTER);
    }

    private void encoder() {
        Object encoderValue = encoderSourceBox.getSelectedItem();
        if (encoderValue == null) return;

        Object targetValue = encoderTargetBox.getSelectedItem();
        if (targetValue == null) return;
        if (leftSource.getSourceTextArea().getText().equalsIgnoreCase("")) return;

        for (Encoder encoder : Encoders.getEncoders()) {
            if (encoder.kind().is(String.valueOf(encoderValue), String.valueOf(targetValue))) {
                rightTarget.getTargetTextArea().setText(encoder.encode(leftSource.getSourceTextArea().getText()));
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
        private JBTextArea targetTextArea = new JBTextArea();

        public RightTarget() {
            super(new BorderLayout());
            add(createFlowLayoutPanel(encoderTargetBox), BorderLayout.NORTH);
            add(new JScrollPane(targetTextArea), BorderLayout.CENTER);
        }

        public JBTextArea getTargetTextArea() {
            return targetTextArea;
        }
    }

    private class LeftSource extends JPanel {
        private JBTextArea sourceTextArea = new JBTextArea();

        public LeftSource() {
            super(new BorderLayout());
            add(createFlowLayoutPanel(encoderSourceBox), BorderLayout.NORTH);
            add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
            sourceTextArea.getDocument().addDocumentListener(EncoderView.this);
        }

        public JBTextArea getSourceTextArea() {
            return sourceTextArea;
        }
    }
}
