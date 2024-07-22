package dev.coolrequest.tool.views.coder.custom;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import dev.coolrequest.tool.common.*;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.components.SimpleFrame;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.GlobalStateManager;
import dev.coolrequest.tool.utils.ComponentUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class EnvAction extends AnAction {

    private final Project project;
    private final ComboBox<String> comboBox;
    private final MultiLanguageTextField multiLanguageFieldText;

    private final Logger logger;
    private final GlobalState globalState;
    private SimpleFrame frame;

    private AtomicBoolean state = new AtomicBoolean(false);

    public EnvAction(Project project, List<Runnable> disposeRegistry) {
        super(() -> I18n.getString("env.title", project), Icons.ENV);
        disposeRegistry.add(() -> {
            Optional.ofNullable(frame).ifPresent(Window::dispose);
        });
        this.globalState = GlobalStateManager.loadState(project);
        String envFileType = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE).orElse(GlobalConstant.ENV_SUPPORT_TYPES[0]);
        LanguageFileType fileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension(envFileType);
        this.project = project;
        this.logger = LogContext.getInstance(project).getLogger(EnvAction.class);
        this.comboBox = new ComboBox<>(GlobalConstant.ENV_SUPPORT_TYPES);
        this.multiLanguageFieldText = new MultiLanguageTextField(fileType, project);
        this.multiLanguageFieldText.setPlaceholder("请点击确认将环境保存,点击关闭或者取消,会导致环境无法保存,请谨慎操作\r\n.json支持json5,可以在json内容中添加注释\r\n.xml使用Properties.loadFromXml(),格式请参考http://java.sun.com/dtd/properties.dtd");
        this.multiLanguageFieldText.setShowPlaceholderWhenFocused(true);
        globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).ifPresent(this.multiLanguageFieldText::setText);
        this.comboBox.setSelectedItem(envFileType);
        this.comboBox.addItemListener(e -> {
            String currentSelectedItem = String.valueOf(e.getItem());
            String selectedItem = String.valueOf(comboBox.getSelectedItem());
            if (!StringUtils.equals(currentSelectedItem, selectedItem)) {
                LanguageFileType selectFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension(selectedItem);
                multiLanguageFieldText.changeLanguageFileType(selectFileType);
            }
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (this.state.compareAndSet(false, true)) {
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(this.multiLanguageFieldText, BorderLayout.CENTER);
            JButton okButton = new JButton(I18n.getString("button.ok", project));
            JButton cancelButton = new JButton(I18n.getString("button.cancel", project));
            JButton applyButton = new JButton(I18n.getString("button.apply", project));
            jPanel.add(ComponentUtils.createFlowLayoutPanel(FlowLayout.RIGHT, this.comboBox, okButton, cancelButton, applyButton), BorderLayout.SOUTH);
            this.frame = new SimpleFrame(jPanel, I18n.getString("env.title", project), new Dimension(800, 600));
            ComponentUtils.addMouseClickListener(applyButton, e -> {
                String text = multiLanguageFieldText.getText();
                //如果缓存有数据,文本没有数据,应该提示用户
                if (StringUtils.isBlank(text) && globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).isPresent()) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器中没有内容,点击确认会覆盖之前设置的环境,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        multiLanguageFieldText.setText("");
                        globalState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        GlobalStateManager.persistence(project);
                        Messages.showInfoMessage("操作成功",I18n.getString("button.apply", project));
                    }
                } else {
                    if (check()) {
                        //有内容,则覆盖
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                        GlobalStateManager.persistence(project);
                        Messages.showInfoMessage("操作成功",I18n.getString("button.apply", project));
                    } else {
                        Messages.showInfoMessage(frame, "文件类型不支持,请检查你的数据和文件类型", "错误提示");
                    }
                }
            });
            ComponentUtils.addMouseClickListener(okButton, e -> {
                String text = multiLanguageFieldText.getText();
                //如果缓存有数据,文本没有数据,应该提示用户
                if (StringUtils.isBlank(text) && globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).isPresent()) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器中没有内容,点击确认会覆盖之前设置的环境,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        multiLanguageFieldText.setText("");
                        globalState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        GlobalStateManager.persistence(project);
                        frame.dispose();
                        state.set(false);
                    }
                } else {
                    if (check()) {
                        //有内容,则覆盖
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                        GlobalStateManager.persistence(project);
                        frame.dispose();
                        state.set(false);
                    } else {
                        Messages.showInfoMessage(frame, "文件类型不支持,请检查你的数据和文件类型", "错误提示");
                    }
                }
            });
            ComponentUtils.addMouseClickListener(cancelButton, e -> {
                String text = multiLanguageFieldText.getText();
                String cacheText = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).orElse("");
                if (!StringUtils.equals(text, cacheText)) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器内容发生改变,点击确定会放弃保存,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        Optional<String> cache = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                        if (cache.isPresent()) {
                            cache.ifPresent(multiLanguageFieldText::setText);
                        } else {
                            multiLanguageFieldText.setText("");
                        }
                        String envFileType = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE).orElse(GlobalConstant.ENV_SUPPORT_TYPES[0]);
                        comboBox.setSelectedItem(envFileType);
                        frame.dispose();
                        state.set(false);
                    }
                } else {
                    String envFileType = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE).orElse(GlobalConstant.ENV_SUPPORT_TYPES[0]);
                    comboBox.setSelectedItem(envFileType);
                    frame.dispose();
                    state.set(false);
                }
            });
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).ifPresent(multiLanguageFieldText::setText);
                    String envFileType = globalState.getOptionalStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE).orElse(GlobalConstant.ENV_SUPPORT_TYPES[0]);
                    comboBox.setSelectedItem(envFileType);
                    state.set(false);
                }
            });
            frame.setVisible(true);
        }else {
            frame.toFront();
        }
    }

    private boolean check() {
        String currentFileType = String.valueOf(this.comboBox.getSelectedItem());
        switch (currentFileType) {
            case "properties": {
                try {
                    String propertiesText = this.multiLanguageFieldText.getText();
                    Properties properties = new Properties();
                    properties.load(new StringReader(propertiesText));
                    Map<String, String> map = properties.entrySet().stream().collect(Collectors.toMap(item -> String.valueOf(item.getKey()), item -> String.valueOf(item.getValue())));
                    this.globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, map);
                    return true;
                } catch (Throwable e) {
                    this.logger.error("文件类型和文件内容不匹配,错误信息: " + e.getMessage());
                    return false;
                }
            }
            case "xml": {
                try {
                    String propertiesText = this.multiLanguageFieldText.getText();
                    Properties properties = new Properties();
                    properties.loadFromXML(new ByteArrayInputStream(propertiesText.getBytes(StandardCharsets.UTF_8)));
                    Map<String, String> map = properties.entrySet().stream().collect(Collectors.toMap(item -> String.valueOf(item.getKey()), item -> String.valueOf(item.getValue())));
                    this.globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, map);
                    return true;
                } catch (Throwable e) {
                    this.logger.error("文件类型和文件内容不匹配,错误信息: " + e.getMessage());
                    return false;
                }
            }
            case "json5": {
                try {
                    JSONObject jsonObject = JSONObject.parse(this.multiLanguageFieldText.getText());
                    this.globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, jsonObject);
                    return true;
                } catch (Throwable e) {
                    this.logger.error("文件类型和文件内容不匹配,错误信息: " + e.getMessage());
                    return false;
                }

            }
            case "yaml": {
                try {
                    Yaml yaml = new Yaml();
                    String yamlText = this.multiLanguageFieldText.getText();
                    this.globalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, yaml.loadAs(yamlText, HashMap.class));
                    return true;
                } catch (Throwable e) {
                    this.logger.error("文件类型和文件内容不匹配,错误信息: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }
}
