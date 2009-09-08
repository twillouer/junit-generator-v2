package org.intellij.plugins.junitgen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;


/**
 * Data holder/distributer object.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 3, 2003</pre>
 */
public class GeneratorContext {
    private DataContext dataContext;
    private PsiJavaFile file;
    private PsiClass psiClass;
    private String outputFileName;

    public GeneratorContext(DataContext ctx, PsiJavaFile file, PsiClass psiClass) {
        this.dataContext = ctx;
        this.file = file;
        this.psiClass = psiClass;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public PsiJavaFile getFile() {
        return file;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public String getPackageName() {
        return this.file.getPackageName();
    }

    public String getClassName(boolean qualified) {
        if (!qualified) {
            return this.psiClass.getName();
        }
        else {
            return this.psiClass.getQualifiedName();
        }
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}
