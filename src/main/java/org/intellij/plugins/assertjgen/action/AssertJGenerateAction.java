package org.intellij.plugins.assertjgen.action;

import java.io.IOException;
import java.io.StringWriter;

import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.Template;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.PsiClassToClassDescriptionConverter;
import org.intellij.plugins.junitgen.JUnitGeneratorContext;
import org.intellij.plugins.junitgen.JUnitGeneratorFileCreator;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * AssertJ Annotation Generator.
 * 
 * @author William Delanoue
 */
public class AssertJGenerateAction extends AnAction {
  private static final Logger logger = JUnitGeneratorUtil.getLogger(AssertJGenerateAction.class);

  @Override
  public void update(AnActionEvent e) {
    PsiJavaFile javaFile = JUnitGeneratorUtil.getSelectedJavaFile(e.getDataContext());
    PsiClass psiClass = getPsiClassFromContext(e);
    e.getPresentation().setEnabled(javaFile != null && psiClass != null);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    PsiClass psiClass = getPsiClassFromContext(e);
    if (psiClass == null) {
      return;
    }
    PsiJavaFile file = JUnitGeneratorUtil.getSelectedJavaFile(e.getDataContext());
    if (file == null) {
      return;
    }

    JUnitGeneratorContext genCtx = new JUnitGeneratorContext(e.getDataContext(), file, psiClass);

    try {
      Project project = e.getData(PlatformDataKeys.PROJECT);
      PsiClassToClassDescriptionConverter classDescriptionConverter = new PsiClassToClassDescriptionConverter(project);
      // Load template vm-template.properties
      BaseAssertionGenerator baseAssertionGenerator = new BaseAssertionGenerator( //
          getTemplate(project, "AssertJ ClassTemplate", Template.Type.ASSERT_CLASS),//
          getTemplate(project, "AssertJ has", Template.Type.HAS), //
          getTemplate(project, "AssertJ hasPrimitive", Template.Type.HAS_FOR_PRIMITIVE), //
          getTemplate(project, "AssertJ Element Iterable", Template.Type.HAS_FOR_ITERABLE), //
          getTemplate(project, "AssertJ Element Array", Template.Type.HAS_FOR_ARRAY), //
          getTemplate(project, "AssertJ Element is", Template.Type.IS));
      ClassDescription classDescription = classDescriptionConverter.convertToClassDescription(psiClass);
      logger.debug("ClassDescription : " + classDescription);
      String content = baseAssertionGenerator.generateCustomAssertionContentFor(classDescription);
      final StringWriter writer = new StringWriter();
      writer.append(content);

      // template.merge(context, writer);
      String outputFileName = classDescription.getClassName() + "Assert";
      ApplicationManager.getApplication().runWriteAction(
          new JUnitGeneratorFileCreator(JUnitGeneratorUtil.resolveOutputFileName(genCtx, outputFileName), writer,
              genCtx));
    } catch (IOException e1) {
      logger.error(e1);
    }
  }

  public Template getTemplate(Project project, String templateKey, Template.Type type) {
    return new Template(type, JUnitGeneratorUtil.getInstance(project).getAssertJTemplate(templateKey));
  }

  private PsiClass getPsiClassFromContext(AnActionEvent e) {
    PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

    Editor editor = e.getData(PlatformDataKeys.EDITOR);
    if (psiFile == null || editor == null) {
      return null;
    }
    int offset = editor.getCaretModel().getOffset();
    PsiElement elementAt = psiFile.findElementAt(offset);
    PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    return psiClass;
  }
}
