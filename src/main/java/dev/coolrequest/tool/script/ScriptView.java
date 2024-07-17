package dev.coolrequest.tool.script;

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
import dev.coolrequest.tool.common.I18n;
import dev.coolrequest.tool.components.MultiLanguageTextField;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

public class ScriptView extends JPanel {


    private Project project;

    private final JBTextArea classPathTextArea = new JBTextArea();


    public ScriptView(Project project) {
        super(new BorderLayout());
        this.project = project;
        Left left = new Left(project);
        Right right = new Right(textArea -> {
            String text = left.getLanguageTextField().getText();
            if (StringUtils.isEmpty(text)) {
                textArea.setText("请输入Groovy脚本");
            } else {
                runScript(text, textArea);
            }
        });
        JBSplitter jbSplitter = new JBSplitter();
        jbSplitter.setSecondComponent(right);
        jbSplitter.setFirstComponent(left);
        this.add(jbSplitter, BorderLayout.CENTER);
    }

    private void runScript(String script, JBTextArea output) {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(ScriptView.class.getClassLoader(), compilerConfiguration)) {
            GroovyShell groovyShell = new GroovyShell(groovyClassLoader, compilerConfiguration);
            for (Library library : LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries()) {
                for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
                    URL url = new File(file.getPresentableUrl()).toURI().toURL();
                    groovyClassLoader.addURL(url);
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
            binding.setVariable("project", project);
            groovyScript.setBinding(binding);
            Object result = groovyScript.run();
            output.setText(String.format("注意事项: \n%s \r\nresult: %s", "只可使用项目依赖的jar包中的对象以及jdk提供的对象,其他不可使用,如需使用,请手动设置classpath,多个通过,或者空行隔开", result));
        } catch (Throwable e) {
            output.setText("groovy脚本执行错误: " + e.getMessage());
        }
    }

    private class Right extends JPanel {
        private final JBTextArea targetTextArea = new JBTextArea();

        public Right(Consumer<JBTextArea> consumer) {
            super(new BorderLayout());
            targetTextArea.setEditable(false);
            JButton button = new JButton(I18n.getString("script.run"));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        consumer.accept(targetTextArea);
                    }
                }
            });
            add(createFlowLayoutPanel(button), BorderLayout.NORTH);
            add(new JScrollPane(targetTextArea), BorderLayout.CENTER);
        }

    }

    private class Left extends JPanel {
        private final LanguageTextField languageTextField;

        public Left(Project project) {
            super(new BorderLayout());
            LanguageFileType groovyFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByExtension("groovy");
            languageTextField = new MultiLanguageTextField(groovyFileType, project);
            JButton button = new JButton("添加classPath");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        JDialog dialog = new JDialog();
                        dialog.setTitle("添加ClassPath");
                        dialog.setLayout(new BorderLayout());
                        dialog.setSize(400, 400);
                        dialog.setLocationRelativeTo(null);
                        dialog.getContentPane().add(new JBScrollPane(classPathTextArea), BorderLayout.CENTER);
                        dialog.setVisible(true);
                    }
                }
            });
            add(createFlowLayoutPanel(button), BorderLayout.NORTH);
            add(languageTextField, BorderLayout.CENTER);
        }

        public LanguageTextField getLanguageTextField() {
            return languageTextField;
        }
    }


    private JPanel createFlowLayoutPanel(JComponent component) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.add(component);
        return jPanel;
    }
}
