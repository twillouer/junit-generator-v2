package org.intellij.plugins.junitgen.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;


/**
 * JUnitGenerator action implementation
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @since <pre>Aug 28, 2003</pre>
 */
public class JUnitGeneratorAction extends EditorAction {

    public JUnitGeneratorAction(String name) {
        super(new JUnitGeneratorActionHandler(name));
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(name, false);
    }

    /**
     * Enables Generate popup for Java files only.
     *
     * @param editor       the editor window we came from
     * @param presentation the presentation window
     * @param dataContext  the data context
     */
    public void update(Editor editor, Presentation presentation, DataContext dataContext) {
        PsiJavaFile javaFile = JUnitGeneratorUtil.getSelectedJavaFile(dataContext);
        presentation.setEnabled(javaFile != null);
    }
}
