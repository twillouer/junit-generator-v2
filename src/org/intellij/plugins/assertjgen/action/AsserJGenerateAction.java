package org.intellij.plugins.assertjgen.action;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.swing.*;

import org.apache.velocity.VelocityContext;
import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.Template;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.PsiClassToClassDescriptionConverter;
import org.intellij.plugins.junitgen.JUnitGeneratorContext;
import org.intellij.plugins.junitgen.JUnitGeneratorFileCreator;
import org.intellij.plugins.junitgen.util.DateTool;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

/**
 * User: william Date: 03/06/13
 */
public class AsserJGenerateAction extends AnAction {
  private static final Logger logger = JUnitGeneratorUtil.getLogger(AsserJGenerateAction.class);

  public static class GenerateDialog extends DialogWrapper {
    private CollectionListModel<PsiField> myFields;
    private final LabeledComponent<JPanel> myComponent;

    public GenerateDialog(PsiClass psiClass) {
      super(psiClass.getProject());
      setTitle("Select Fields for ComparisonChain");

      myFields = new CollectionListModel<PsiField>(psiClass.getAllFields());
      JBList fieldList = new JBList(myFields);
      fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
      ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
      decorator.disableAddAction();
      JPanel panel = decorator.createPanel();
      myComponent = LabeledComponent.create(panel, "Fields to include in compareTo():");

      init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return myComponent;
    }

    public List<PsiField> getFields() {
      return myFields.getItems();
    }
  }

  // public void actionPerformed(AnActionEvent event) {
  // Project project = event.getData(PlatformDataKeys.PROJECT);
  // VirtualFile currentFile = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
  //
  // String txt = Messages.showInputDialog(project, "What is your name?", "Input your name",
  // Messages.getQuestionIcon());
  // Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information",
  // Messages.getInformationIcon());
  // }

  public void actionPerformed(AnActionEvent e) {
    PsiClass psiClass = getPsiClassFromContext(e);
    if (psiClass == null) {
      return;
    }
    PsiJavaFile file = JUnitGeneratorUtil.getSelectedJavaFile(e.getDataContext());
    if (file == null) {
      return;
    }

    // PsiClass[] innerClass = psiClass.getAllInnerClasses();

    // for (PsiClass innerClas : innerClass) {
    //
    // buildMethodList(innerClas.getMethods(), methodList, !getPrivate);
    // buildMethodList(innerClas.getMethods(), pMethodList, getPrivate);
    // buildFieldList(psiClass.getFields(), fieldList);
    // }
    //
    Project project = e.getData(PlatformDataKeys.PROJECT);

    Messages.showMessageDialog(project, "Hello, " + file.getName() + "!\n I am glad to see you.", "Information",
        Messages.getInformationIcon());
    // generateComparable(psiClass, dlg.getFields());
    // }

    final JUnitGeneratorContext genCtx = new JUnitGeneratorContext(e.getDataContext(), file, psiClass);
    final VelocityContext context = new VelocityContext();
    // context.put("entryList", entryList);
    context.put("today", JUnitGeneratorUtil.formatDate("MM/dd/yyyy"));
    context.put("date", new DateTool());

    // final Template template = ve.getTemplate(VIRTUAL_TEMPLATE_NAME);

    try {
      PsiClassToClassDescriptionConverter classDescriptionConverter = new PsiClassToClassDescriptionConverter(project);
      BaseAssertionGenerator baseAssertionGenerator = new BaseAssertionGenerator( //
          getTemplate(project, "AssertJ ClassTemplate", Template.Type.ASSERT_CLASS),//
          getTemplate(project, "AssertJ has", Template.Type.HAS), //
          getTemplate(project, "AssertJ Element Iterable", Template.Type.HAS_FOR_ITERABLE), //
          getTemplate(project, "AssertJ Element Array", Template.Type.HAS_FOR_ARRAY), //
          getTemplate(project, "AssertJ Element is", Template.Type.IS));
      logger.info("psiClass : " + psiClass);
      ClassDescription classDescription = classDescriptionConverter.convertToClassDescription(psiClass);
      logger.info("ClassDescription : " + classDescription);
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
    return new Template(type, JUnitGeneratorUtil.getInstance(project).getTemplate(templateKey));
  }

  public void generateComparable(final PsiClass psiClass, final List<PsiField> fields) {
    new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {

      @Override
      protected void run() throws Throwable {
        generateCompareTo(psiClass, fields);
        generateImplementsComparable(psiClass);

      }

    }.execute();
  }

  private void generateImplementsComparable(PsiClass psiClass) {
    PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();
    for (PsiClassType implementsListType : implementsListTypes) {
      PsiClass resolved = implementsListType.resolve();
      if (resolved != null && "java.lang.Comparable".equals(resolved.getQualifiedName())) {
        return;
      }
    }

    String implementsType = "Comparable<" + psiClass.getName() + ">";
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiJavaCodeReferenceElement referenceElement = elementFactory.createReferenceFromText(implementsType, psiClass);
    psiClass.getImplementsList().add(referenceElement);
  }

  private void generateCompareTo(PsiClass psiClass, List<PsiField> fields) {
    StringBuilder builder = new StringBuilder("public int compareTo(");
    builder.append(psiClass.getName()).append(" that) {\n");
    builder.append("return COUCOU.start()");
    for (PsiField field : fields) {
      builder.append(".compare(this.").append(field.getName()).append(", that.");
      builder.append(field.getName()).append(")");
    }
    builder.append(".result();\n}");
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiMethod compareTo = elementFactory.createMethodFromText(builder.toString(), psiClass);
    PsiElement method = psiClass.add(compareTo);
    JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);
  }

  @Override
  public void update(AnActionEvent e) {
    PsiJavaFile javaFile = JUnitGeneratorUtil.getSelectedJavaFile(e.getDataContext());
    e.getPresentation().setEnabled(javaFile != null);
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
