package dev.coolrequest.tool.components;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import dev.coolrequest.tool.common.LogContext;
import dev.coolrequest.tool.common.Logger;
import org.jetbrains.annotations.NotNull;

public class MultiLanguageTextField extends LanguageTextField {

    private final SimpleDocumentCreator documentCreator;
    private final Logger logger;

    private LanguageFileType languageFileType;

    public MultiLanguageTextField(LanguageFileType languageFileType, Project project) {
        this(languageFileType, project, new SimpleDocumentCreator());
    }

    public MultiLanguageTextField(LanguageFileType languageFileType, Project project, SimpleDocumentCreator documentCreator) {
        super(languageFileType.getLanguage(), project, "", documentCreator, false);
        this.documentCreator = documentCreator;
        this.languageFileType = languageFileType;
        logger = LogContext.getInstance(project).getLogger(MultiLanguageTextField.class);
    }


    public void changeLanguageFileType(LanguageFileType languageFileType) {
        if (this.languageFileType != languageFileType) {
            this.setNewDocumentAndFileType(languageFileType, this.documentCreator.createDocument(this.getDocument().getText(), languageFileType.getLanguage(), this.getProject()));
            this.languageFileType = languageFileType;
            Editor editor = this.getEditor();
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(HighlighterFactory.createHighlighter(this.getProject(), this.languageFileType));
            }
        }
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        editor.setHighlighter(HighlighterFactory.createHighlighter(this.getProject(), this.languageFileType));
        editor.setEmbeddedIntoDialogWrapper(true);
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setRightMarginShown(true);
        settings.setAutoCodeFoldingEnabled(false);
        return editor;
    }
}
