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
import dev.coolrequest.tool.state.ProjectState;
import dev.coolrequest.tool.state.ProjectStateManager;
import dev.coolrequest.tool.state.Scope;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class EnvAction extends AnAction {

    private final Project project;
    private final ComboBox<String> comboBox;
    private final MultiLanguageTextField multiLanguageFieldText;

    private final Logger logger;
    private final ProjectState projectState;
    private SimpleFrame frame;

    private final AtomicBoolean state = new AtomicBoolean(false);

    public EnvAction(Project project, List<Runnable> disposeRegistry) {
        super(() -> I18n.getString("env.title", project), Icons.ENV);
        disposeRegistry.add(() -> {
            Optional.ofNullable(frame).ifPresent(Window::dispose);
        });
        this.projectState = ProjectStateManager.load(project);
        String envFileType = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, GlobalConstant.ENV_SUPPORT_TYPES[0]);
        LanguageFileType fileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension(envFileType);
        this.project = project;
        this.logger = LogContext.getInstance(project).getLogger(EnvAction.class);
        this.comboBox = new ComboBox<>(GlobalConstant.ENV_SUPPORT_TYPES);
        this.multiLanguageFieldText = new MultiLanguageTextField(fileType, project);
        this.multiLanguageFieldText.setPlaceholder("请点击确认将环境保存,点击关闭或者取消,会导致环境无法保存,请谨慎操作\r\n.json支持json5,可以在json内容中添加注释\r\n.xml使用Properties.loadFromXml(),格式请参考http://java.sun.com/dtd/properties.dtd");
        this.multiLanguageFieldText.setShowPlaceholderWhenFocused(true);
        String envText = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, "");
        this.multiLanguageFieldText.setText(envText);
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
            this.multiLanguageFieldText.setText(getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, ""));
            this.comboBox.setSelectedItem(getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, GlobalConstant.ENV_SUPPORT_TYPES[0]));
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(this.multiLanguageFieldText, BorderLayout.CENTER);
            JButton okButton = new JButton(I18n.getString("button.ok", project));
            JButton cancelButton = new JButton(I18n.getString("button.cancel", project));
            JButton applyButton = new JButton(I18n.getString("button.apply", project));
            jPanel.add(ComponentUtils.createFlowLayoutPanel(FlowLayout.RIGHT, this.comboBox, okButton, cancelButton, applyButton), BorderLayout.SOUTH);
            this.frame = new SimpleFrame(jPanel, I18n.getString("env.title", project), new Dimension(800, 600));
            ComponentUtils.addMouseClickListener(applyButton, e -> {
                String text = multiLanguageFieldText.getText();
                //获取环境变量
                String envText = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, "");
                //如果缓存有数据,文本没有数据,应该提示用户
                if (StringUtils.isBlank(text) && StringUtils.isNotBlank(envText)) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器中没有内容,点击确认会覆盖之前设置的环境,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        multiLanguageFieldText.setText("");
                        //持久化环境变量
                        if (projectState.getScope() == Scope.PROJECT) {
                            projectState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            ProjectStateManager.store(project);
                        } else {
                            GlobalState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        }
                        Messages.showInfoMessage("操作成功", I18n.getString("button.apply", project));
                    }
                } else {
                    if (check()) {
                        if (projectState.getScope() == Scope.PROJECT) {
                            //有内容,则覆盖
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                            ProjectStateManager.store(project);
                        } else {
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                        }
                        Messages.showInfoMessage("操作成功", I18n.getString("button.apply", project));
                    } else {
                        Messages.showInfoMessage(frame, "文件类型不支持,请检查你的数据和文件类型", "错误提示");
                    }
                }
            });
            ComponentUtils.addMouseClickListener(okButton, e -> {
                String text = multiLanguageFieldText.getText();
                //获取环境变量
                String envText = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, "");
                //如果缓存有数据,文本没有数据,应该提示用户
                if (StringUtils.isBlank(text) && StringUtils.isNotBlank(envText)) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器中没有内容,点击确认会覆盖之前设置的环境,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        multiLanguageFieldText.setText("");
                        if (projectState.getScope() == Scope.PROJECT) {
                            projectState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            ProjectStateManager.store(project);
                        } else {
                            GlobalState.removeCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                        }
                        frame.dispose();
                        state.set(false);
                    }
                } else {
                    if (check()) {
                        //有内容,则覆盖
                        if (projectState.getScope() == Scope.PROJECT) {
                            //有内容,则覆盖
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            projectState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                            ProjectStateManager.store(project);
                        } else {
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, this.comboBox.getSelectedItem());
                            GlobalState.putCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, text);
                        }
                        frame.dispose();
                        state.set(false);
                    } else {
                        Messages.showInfoMessage(frame, "文件类型不支持,请检查你的数据和文件类型", "错误提示");
                    }
                }
            });
            ComponentUtils.addMouseClickListener(cancelButton, e -> {
                String text = multiLanguageFieldText.getText();
                String cacheText = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT, "");
                if (!StringUtils.equals(text, cacheText)) {
                    int i = JOptionPane.showConfirmDialog(frame, "当前编辑器内容发生改变,点击确定会放弃保存,请谨慎操作", "警告", JOptionPane.OK_CANCEL_OPTION);
                    if (i == JOptionPane.OK_OPTION) {
                        Optional<String> cache = getScopeOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT);
                        String envFileType = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, GlobalConstant.ENV_SUPPORT_TYPES[0]);
                        if (cache.isPresent()) {
                            cache.ifPresent(multiLanguageFieldText::setText);
                        } else {
                            multiLanguageFieldText.setText("");
                        }
                        comboBox.setSelectedItem(envFileType);
                        frame.dispose();
                        state.set(false);
                    }
                } else {
                    String envFileType = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, GlobalConstant.ENV_SUPPORT_TYPES[0]);
                    comboBox.setSelectedItem(envFileType);
                    frame.dispose();
                    state.set(false);
                }
            });
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    getScopeOpStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TEXT).ifPresent(multiLanguageFieldText::setText);
                    String envFileType = getScopeStrCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT_TYPE, GlobalConstant.ENV_SUPPORT_TYPES[0]);
                    comboBox.setSelectedItem(envFileType);
                    state.set(false);
                }
            });
            frame.setVisible(true);
        } else {
            frame.toFront();
        }
    }

    public Optional<String> getScopeOpStrCache(String key) {
        if (projectState.getScope() == Scope.PROJECT) {
            return projectState.getOpStrCache(key);
        }
        return GlobalState.getOpStrCache(key);
    }

    public String getScopeStrCache(String key, String defaultValue) {
        if (projectState.getScope() == Scope.PROJECT) {
            return projectState.getOpStrCache(key).orElse(defaultValue);
        }
        return GlobalState.getOpStrCache(key).orElse(defaultValue);
    }

    public void scopePutCache(String key, Object data) {
        if (projectState.getScope() == Scope.PROJECT) {
            projectState.putCache(key, data);
        } else {
            GlobalState.putCache(key, data);
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
                    scopePutCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, map);
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
                    scopePutCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, map);
                    return true;
                } catch (Throwable e) {
                    this.logger.error("文件类型和文件内容不匹配,错误信息: " + e.getMessage());
                    return false;
                }
            }
            case "json5": {
                try {
                    JSONObject jsonObject = JSONObject.parse(this.multiLanguageFieldText.getText());
                    scopePutCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, jsonObject);
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
                    scopePutCache(CacheConstant.CODER_VIEW_CUSTOM_CODER_ENVIRONMENT, yaml.loadAs(yamlText, HashMap.class));
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
