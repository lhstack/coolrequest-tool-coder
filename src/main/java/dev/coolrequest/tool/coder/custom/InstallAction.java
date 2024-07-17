package dev.coolrequest.tool.coder.custom;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.common.Icons;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class InstallAction extends AnAction {
    private final MultiLanguageTextField codeTextField;
    private final JBTextArea outputTextField;
    private final GroovyShell groovyShell;
    private final JComboBox<String> coderSourceBox;
    private final JComboBox<String> coderTargetBox;
    private final List<Coder> baseCoders;
    private final List<Coder> dynamicCoders;

    public InstallAction(MultiLanguageTextField codeTextField, JBTextArea outputTextField, GroovyShell groovyShell, JComboBox<String> coderSourceBox, JComboBox<String> coderTargetBox, List<Coder> baseCoders, List<Coder> dynamicCoders) {
        super(() -> I18n.getString("coder.custom.install"), Icons.INSTALL_ACTION);
        this.codeTextField = codeTextField;
        this.outputTextField = outputTextField;
        this.groovyShell = groovyShell;
        this.coderSourceBox = coderSourceBox;
        this.coderTargetBox = coderTargetBox;
        this.baseCoders = baseCoders;
        this.dynamicCoders = dynamicCoders;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        String code = this.codeTextField.getText();
        if (StringUtils.isNotBlank(code)) {
            //是否发生修改
            AtomicBoolean hasChanged = new AtomicBoolean(false);
            //临时添加的coders
            List<Coder> tempCoders = new ArrayList<>();

            StringBuilder logBuffer = new StringBuilder();
            Consumer<Object> log = s -> {
                logBuffer.append(s).append("\n");
            };
            class CoderRegistry {
                public void registry(String source, String target, Function<String, String> mapper) {
                    Coder coder = new Coder() {
                        @Override
                        public String transform(String data) {
                            return mapper.apply(data);
                        }

                        @Override
                        public Kind kind() {
                            return Kind.of(source, target);
                        }
                    };
                    for (Coder dynamicCoder : dynamicCoders) {
                        if (!dynamicCoder.kind().is(coder.kind().source, coder.kind().target)) {
                            tempCoders.add(coder);
                            hasChanged.set(true);
                        } else {
                            log.accept(String.format("存在重名的coder,请修改coder类型重新注册: source=%s,target=%s", dynamicCoder.kind().source, dynamicCoder.kind().target));
                        }
                    }
                }
            }
            CoderRegistry coderRegistry = new CoderRegistry();
            Binding binding = new Binding();
            binding.setVariable("coder", coderRegistry);
            binding.setVariable("log", log);
            Script script = this.groovyShell.parse(code);
            script.setBinding(binding);
            script.run();
            if (hasChanged.get()) {
                dynamicCoders.clear();
                dynamicCoders.addAll(this.baseCoders);
                dynamicCoders.addAll(tempCoders);
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

            } else {
                log.accept("所有coder都已经重复了,不需要重新注册");
            }
            if (logBuffer.length() > 0) {
                outputTextField.setText(logBuffer.toString());
            }

        }
    }
}
