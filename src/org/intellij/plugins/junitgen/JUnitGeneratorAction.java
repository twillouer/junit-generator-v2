package org.intellij.plugins.junitgen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.junitgen.util.GenUtil;


/**
 * JUnitGenerator action implementation
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 28, 2003</pre>
 */
public class JUnitGeneratorAction extends EditorAction {
    public JUnitGeneratorAction() {
        super(new JUnitGeneratorActionHandler());
    }

    /**
     * Enables Generate popup for Java files only.
     * @param editor
     * @param presentation
     * @param dataContext
     */
    public void update(Editor editor, Presentation presentation, DataContext dataContext) {
        PsiJavaFile javaFile = GenUtil.getSelectedJavaFile(dataContext);
        presentation.setEnabled(javaFile != null);
    }
}
