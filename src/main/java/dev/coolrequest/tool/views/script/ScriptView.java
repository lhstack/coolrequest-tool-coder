package dev.coolrequest.tool.views.script;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import dev.coolrequest.tool.common.*;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import dev.coolrequest.tool.components.SimpleFrame;
import dev.coolrequest.tool.state.GlobalState;
import dev.coolrequest.tool.state.GlobalStateManager;
import dev.coolrequest.tool.utils.ClassLoaderUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ScriptView extends JPanel {

    private final Logger logger;
    private Project project;

    private final JBTextArea classPathTextArea = new JBTextArea();


    public ScriptView(Project project) {
        super(new BorderLayout());
        this.project = project;
        logger = LogContext.getInstance(project).getLogger(ScriptView.class);
        GlobalStateManager.loadState(project).getOptionalStrCache(CacheConstant.SCRIPT_VIEW_CACHE_CLASSPATH).ifPresent(classPathTextArea::setText);
        Left left = new Left(project);
        Right right = new Right(textArea -> {
            String text = left.getLanguageTextField().getText();
            if (StringUtils.isEmpty(text)) {
                textArea.setText("请输入Groovy脚本");
            } else {
                runScript(text, textArea);
            }
        }, project);
        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setSecondComponent(right);
        jbSplitter.setFirstComponent(left);
        this.add(jbSplitter, BorderLayout.CENTER);
    }

    private void runScript(String script, JBTextArea output) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        Logger scriptLogger = new TextAreaLogger("groovy.script", output);
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(ScriptView.class.getClassLoader(), compilerConfiguration)) {
            GroovyShell groovyShell = new GroovyShell(groovyClassLoader, compilerConfiguration);
            GlobalState globalState = GlobalStateManager.loadState(project);
            if (globalState.getBooleanCache(CacheConstant.SCRIPT_VIEW_CACHE_USING_PROJECT_LIBRARY)) {
                for (Library library : LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries()) {
                    for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
                        URL url = new File(file.getPresentableUrl()).toURI().toURL();
                        groovyClassLoader.addURL(url);
                    }
                }
            }
            String classPaths = classPathTextArea.getText();
            if (StringUtils.isNotBlank(classPaths)) {
                String[] classPathArray = classPaths.split("\n");
                for (String classPathItems : classPathArray) {
                    for (String classPath : classPathItems.split(",")) {
                        if (StringUtils.isNotBlank(StringUtils.trimToEmpty(classPath))) {
                            groovyClassLoader.addURL(new File(StringUtils.trimToEmpty(classPath)).toURI().toURL());
                        }
                    }
                }
            }
            Script groovyScript = groovyShell.parse(script);
            Binding binding = new Binding();
            binding.setVariable("log", scriptLogger);
            binding.setVariable("sysLog", logger);
            groovyScript.setBinding(binding);
            FutureTask<Object> futureTask = new FutureTask<>(groovyScript::run);
            try {
                Thread thread = new Thread(futureTask);
                thread.start();
                futureTask.get(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                futureTask.cancel(true);
                scriptLogger.error("脚本执行失败,错误信息: " + e);
            }
            GlobalStateManager.loadState(project).putCache(CacheConstant.SCRIPT_VIEW_CACHE_CODE, script);
            GlobalStateManager.persistence(project);
        } catch (Throwable e) {
            scriptLogger.error("groovy脚本执行错误: " + e.getMessage() + "\n");
        }
    }

    private class Right extends JPanel {
        private final JBTextArea targetTextArea = new JBTextArea();

        public Right(Consumer<JBTextArea> consumer, Project project) {
            super(new BorderLayout());
            targetTextArea.setEditable(false);
            targetTextArea.setText(String.format("注意事项: \n%s\n", "只可使用项目依赖的jar包中的对象以及jdk提供的对象,其他不可使用,如需使用,请手动设置classpath,多个通过,或者空行隔开"));
            JButton button = new JButton(I18n.getString("script.run", project));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        consumer.accept(targetTextArea);
                    }
                }
            });
            JButton clearLogButton = new JButton(I18n.getString("script.clearLog", project));
            clearLogButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        targetTextArea.setText(String.format("注意事项: \n%s\n", "只可使用项目依赖的jar包中的对象以及jdk提供的对象,其他不可使用,如需使用,请手动设置classpath,多个通过,或者空行隔开"));
                    }
                }
            });
            add(createFlowLayoutPanel(button, clearLogButton), BorderLayout.NORTH);
            add(new JScrollPane(targetTextArea), BorderLayout.CENTER);
        }

    }

    private class Left extends JPanel {
        private final LanguageTextField languageTextField;

        public Left(Project project) {
            super(new BorderLayout());
            LanguageFileType groovyFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension("groovy");
            languageTextField = new MultiLanguageTextField(groovyFileType, project);
            GlobalStateManager.loadState(project).getOptionalStrCache(CacheConstant.SCRIPT_VIEW_CACHE_CODE).ifPresent(languageTextField::setText);
            JButton button = new JButton(I18n.getString("script.addclasspath.title", project));
            button.addMouseListener(new MouseAdapter() {

                private SimpleFrame frame;
                private final AtomicBoolean state = new AtomicBoolean(false);

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (state.compareAndSet(false, true)) {
                            this.frame = new SimpleFrame(new JBScrollPane(classPathTextArea), I18n.getString("script.addclasspath.title", project), new Dimension(600, 400));
                            frame.setVisible(true);
                            frame.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent e) {
                                    if (StringUtils.isNotBlank(classPathTextArea.getText())) {
                                        GlobalStateManager.loadState(project).putCache(CacheConstant.SCRIPT_VIEW_CACHE_CLASSPATH, classPathTextArea.getText());
                                        GlobalStateManager.persistence(project);
                                    }
                                    state.set(false);
                                }
                            });
                        }else {
                            frame.toFront();
                        }
                    }
                }
            });
            JButton templateCodeButton = new JButton(I18n.getString("script.code.template", project));
            String templateCode = ClassLoaderUtils.getResourceToString("template/ScriptTemplateCode.groovy");
            templateCodeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        String script = languageTextField.getText();
                        if (StringUtils.isNotBlank(script)) {
                            if (!StringUtils.equals(script, templateCode)) {
                                int i = JOptionPane.showConfirmDialog(null, "点击确定会覆盖原有代码,请谨慎操作", "警告", JOptionPane.YES_NO_OPTION);
                                if (i == JOptionPane.YES_OPTION) {
                                    languageTextField.setText(templateCode);
                                }
                            }
                        } else {
                            String templateCode = ClassLoaderUtils.getResourceToString("template/ScriptTemplateCode.groovy");
                            languageTextField.setText(templateCode);
                        }
                    }
                }
            });
            DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
            GlobalState globalState = GlobalStateManager.loadState(project);
            defaultActionGroup.add(new ToggleAction(() -> I18n.getString("script.usingProjectLibrary", project), Icons.LIBRARY) {

                @Override
                public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                    return globalState.getBooleanCache(CacheConstant.SCRIPT_VIEW_CACHE_USING_PROJECT_LIBRARY);
                }

                @Override
                public void setSelected(@NotNull AnActionEvent event, boolean state) {
                    boolean currentState = globalState.getBooleanCache(CacheConstant.SCRIPT_VIEW_CACHE_USING_PROJECT_LIBRARY);
                    if (currentState != state) {
                        globalState.putCache(CacheConstant.SCRIPT_VIEW_CACHE_USING_PROJECT_LIBRARY, state);
                        GlobalStateManager.persistence(project);
                    }
                }
            });
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("ScriptView", defaultActionGroup, true);
            add(createFlowLayoutPanel(button, templateCodeButton, actionToolbar.getComponent()), BorderLayout.NORTH);
            add(languageTextField, BorderLayout.CENTER);
        }

        public LanguageTextField getLanguageTextField() {
            return languageTextField;
        }
    }


    private JPanel createFlowLayoutPanel(JComponent... components) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JComponent component : components) {
            jPanel.add(component);
        }
        return jPanel;
    }
}
