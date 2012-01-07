package org.intellij.plugins.junitgen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.psi.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.intellij.plugins.junitgen.bean.MethodComposite;
import org.intellij.plugins.junitgen.bean.TemplateEntry;
import org.intellij.plugins.junitgen.util.DateTool;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;
import org.intellij.plugins.junitgen.util.LogAdapter;

import java.io.StringWriter;
import java.util.*;

/**
 * This is where the magic happens.
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @author By: Bryan Gilbert, July 18, 2008
 * @since <pre>Sep 3, 2003</pre>
 */
public class JUnitGeneratorActionHandler extends EditorWriteActionHandler {

    private static final Logger logger = JUnitGeneratorUtil.getLogger(JUnitGeneratorActionHandler.class);

    private static final String VIRTUAL_TEMPLATE_NAME = "junitgenerator.vm";

    /**
     * Executed upon action in the Editor
     *
     * @param editor      IDEA Editor
     * @param dataContext DataCOntext
     */
    public void executeWriteAction(Editor editor, DataContext dataContext) {
        PsiJavaFile file = JUnitGeneratorUtil.getSelectedJavaFile(dataContext);

        if (file == null) {
            return;
        }

        PsiClass[] psiClasses = file.getClasses();

        if (psiClasses == null) {
            return;
        }

        for (PsiClass psiClass : psiClasses) {
            if ((psiClass != null) && (psiClass.getQualifiedName() != null)) {
                final GeneratorContext genCtx = new GeneratorContext(dataContext, file, psiClass);
                final List<TemplateEntry> entryList = new ArrayList<TemplateEntry>();

                try {

                    if (!psiClass.isInterface()) {
                        boolean getPrivate = true;

                        List<PsiMethod> methodList = new ArrayList<PsiMethod>();
                        List<PsiMethod> pMethodList = new ArrayList<PsiMethod>();
                        List<String> fieldList = new ArrayList<String>();

                        List<MethodComposite> methodCompositeList = new ArrayList<MethodComposite>();
                        List<MethodComposite> privateMethodCompositeList = new ArrayList<MethodComposite>();

                        buildMethodList(psiClass.getMethods(), methodList, !getPrivate);
                        buildMethodList(psiClass.getMethods(), pMethodList, getPrivate);
                        buildFieldList(psiClass.getFields(), fieldList);
                        PsiClass[] innerClass = psiClass.getAllInnerClasses();

                        for (PsiClass innerClas : innerClass) {
                            buildMethodList(innerClas.getMethods(), methodList, !getPrivate);
                            buildMethodList(innerClas.getMethods(), pMethodList, getPrivate);
                            buildFieldList(psiClass.getFields(), fieldList);
                        }

                        processMethods(genCtx, methodList, methodCompositeList);
                        processMethods(genCtx, pMethodList, privateMethodCompositeList);

                        entryList.add(new TemplateEntry(genCtx.getClassName(false),
                                genCtx.getPackageName(),
                                methodCompositeList,
                                privateMethodCompositeList,
                                fieldList));
                        process(genCtx, entryList);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }


    /**
     * Creates a list of methods with set and get methods combined together.
     *
     * @param genCtx              the generator context
     * @param methodList          list of methods to process
     * @param methodCompositeList the composite list
     */
    private void processMethods(GeneratorContext genCtx, List<PsiMethod> methodList, List<MethodComposite> methodCompositeList) {
        List<String> methodNames = new ArrayList<String>();
        List<MethodComposite> methodComposites;

        methodComposites = convertToComposites(genCtx, methodList);

        if (JUnitGeneratorSettings.getInstance(genCtx.getProject()).isGenerateForOverloadedMethods()) {
            methodComposites = updateOverloadedMethods(genCtx, methodComposites);
        }

        for (MethodComposite method : methodComposites) {
            String methodName = method.getName();

            if (JUnitGeneratorSettings.getInstance(genCtx.getProject()).isCombineGetterAndSetter() &&
                    methodName.startsWith("set") || methodName.startsWith("get") || methodName.startsWith("is")) {
                methodName = parseAccessorMutator(methodName, methodList);
            }

            if (!methodNames.contains(methodName)) {
                methodNames.add(methodName);
                method.setName(methodName);
                methodCompositeList.add(method);
            }
        }
    }

    /**
     * Create a MethodComposite object for each of the methods passed in
     *
     * @param genCtx     the context
     * @param methodList the method list
     * @return the list of methods
     */
    private List<MethodComposite> convertToComposites(GeneratorContext genCtx, List<PsiMethod> methodList) {

        List<MethodComposite> compositeList = new ArrayList<MethodComposite>();

        for (PsiMethod method : methodList) {

            List<String> paramClassList = new ArrayList<String>();
            for (PsiParameter param : method.getParameterList().getParameters()) {
                String className = (new StringTokenizer(param.getText(), " ")).nextToken();
                paramClassList.add(className);
            }

            List<String> paramNameList = new ArrayList<String>();
            for (PsiParameter param : method.getParameterList().getParameters()) {
                paramNameList.add(param.getName());
            }

            String signature = createSignature(method);

            List<String> reflectionCode = createReflectionCode(genCtx, method);

            MethodComposite composite = new MethodComposite();
            composite.setMethod(method);
            composite.setName(method.getName());
            composite.setParamClasses(paramClassList);
            composite.setParamNames(paramNameList);
            composite.setReflectionCode(reflectionCode);
            composite.setSignature(signature);

            compositeList.add(composite);
        }

        return compositeList;
    }

    private String createSignature(PsiMethod method) {

        String signature;
        String params = "";

        for (PsiParameter param : method.getParameterList().getParameters()) {
            params += param.getText() + ", ";
        }

        if (params.endsWith(", ")) {
            params = params.substring(0, params.length() - 2);
        }

        signature = method.getName() + "(" + params + ")";

        return signature;

    }

    private List<String> createReflectionCode(GeneratorContext genCtx, PsiMethod method) {

        String getMethodText = "\"" + method.getName() + "\"";

        for (PsiParameter param : method.getParameterList().getParameters()) {
            String className = (new StringTokenizer(param.getText(), " ")).nextToken();
            getMethodText = getMethodText + ", " + className + ".class";
        }

        List<String> reflectionCode = new ArrayList<String>();
        reflectionCode.add("/*");
        reflectionCode.add("try {");
        reflectionCode.add("   Method method = " + genCtx.getClassName(false) + ".getClass().getMethod(" + getMethodText + ");");
        reflectionCode.add("   method.setAccessible(true);");
        reflectionCode.add("   method.invoke(<Object>, <Parameters>);");
        reflectionCode.add("} catch(NoSuchMethodException e) {");
        reflectionCode.add("} catch(IllegalAccessException e) {");
        reflectionCode.add("} catch(InvocationTargetException e) {");
        reflectionCode.add("}");
        reflectionCode.add("*/");

        return reflectionCode;
    }

    private List<MethodComposite> updateOverloadedMethods(GeneratorContext context, List<MethodComposite> methodList) {

        HashMap<String, Integer> methodNameMap = new HashMap<String, Integer>();
        HashMap<String, Integer> overloadMethodNameMap = new HashMap<String, Integer>();

        for (MethodComposite method : methodList) {
            String methodName = method.getName();
            if (!methodNameMap.containsKey(methodName)) {
                methodNameMap.put(methodName, 1);
            } else {
                Integer count = methodNameMap.get(methodName);
                methodNameMap.remove(methodName);
                methodNameMap.put(methodName, count + 1);
            }
        }

        for (String key : methodNameMap.keySet()) {
            if (methodNameMap.get(key) > 1) {
                overloadMethodNameMap.put(key, methodNameMap.get(key));
            }
        }

        for (int i = 0; i < methodList.size(); i++) {

            MethodComposite method = methodList.get(i);
            String methodName = method.getName();
            if (overloadMethodNameMap.containsKey(methodName)) {
                int count = overloadMethodNameMap.get(methodName);
                overloadMethodNameMap.remove(methodName);
                overloadMethodNameMap.put(methodName, count - 1);
                methodList.set(i, mutateOverloadedMethodName(context, method, count));
            }
        }

        return methodList;
    }

    private MethodComposite mutateOverloadedMethodName(GeneratorContext context, MethodComposite method, int count) {

        String stringToAppend = "";
        final String overloadType = JUnitGeneratorSettings.getInstance(context.getProject()).getListOverloadedMethodsBy();

        if (JUnitGeneratorUtil.NUMBER.equalsIgnoreCase(overloadType)) {
            stringToAppend += count;
        } else if (JUnitGeneratorUtil.PARAM_CLASS.equalsIgnoreCase(overloadType)) {

            if (method.getParamClasses().size() > 1) {
                stringToAppend += "For";
            }

            for (String paramClass : method.getParamClasses()) {
                paramClass = paramClass.substring(0, 1).toUpperCase() + paramClass.substring(1, paramClass.length());
                stringToAppend += paramClass;
            }
        } else if (JUnitGeneratorUtil.PARAM_NAME.equalsIgnoreCase(overloadType)) {

            if (method.getParamNames().size() > 1) {
                stringToAppend += "For";
            }

            for (String paramName : method.getParamNames()) {
                paramName = paramName.substring(0, 1).toUpperCase() + paramName.substring(1, paramName.length());
                stringToAppend += paramName;
            }
        }

        method.setName(method.getName() + stringToAppend);

        return method;
    }

    /**
     * This method takes in an accessor or mutator method that is named using get*, set*, or is* and combines
     * the method name to provide one method name: "GetSet<BaseName>"
     *
     * @param methodName - Name of accessor or mutator method
     * @param methodList - Entire list of method using to create test
     * @return String updated method name if list contains both accessor and modifier for base name
     */
    private String parseAccessorMutator(String methodName, List methodList) {

        String baseName;

        if (methodName.startsWith("is")) {
            baseName = methodName.substring(2);
        } else {
            baseName = methodName.substring(3);
        }

        if (methodList.contains("get" + baseName) && (methodList.contains("set" + baseName) || methodList.contains("is" + baseName))) {
            methodName = "GetSet" + baseName;
        }

        return methodName;
    }

    /**
     * Builds a list of class scope fields from an array of PsiFields
     *
     * @param fields    an array of fields
     * @param fieldList list to be populated
     */
    private void buildFieldList(PsiField[] fields, List<String> fieldList) {
        for (PsiField field : fields) {
            fieldList.add(field.getName());
        }
    }

    /**
     * Builds method List from an array of PsiMethods
     *
     * @param methods    array of methods
     * @param methodList list to be populated
     * @param getPrivate boolean value, if true returns only private methods, if false only returns none private methods
     */
    private void buildMethodList(PsiMethod[] methods, List<PsiMethod> methodList, boolean getPrivate) {

        for (PsiMethod method : methods) {
            if (!method.isConstructor()) {
                PsiModifierList modifiers = method.getModifierList();

                if ((!modifiers.hasModifierProperty("private") && !getPrivate) || (modifiers.hasModifierProperty("private") && getPrivate)) {
                    methodList.add(method);
                }
            }
        }
    }

    /**
     * Sets all the needed vars in VelocityContext and
     * merges the template
     *
     * @param genCtx    the context
     * @param entryList the list of entries to go into velocity scope
     */
    protected void process(GeneratorContext genCtx, List<TemplateEntry> entryList) {
        try {
            final Properties velocityProperties = new Properties();
            //use the 'string' resource loader because the template comes from a 'string'
            velocityProperties.setProperty(VelocityEngine.RESOURCE_LOADER, "string");
            velocityProperties.setProperty("string.resource.loader.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
            velocityProperties.setProperty("string.resource.loader.repository.class", "org.apache.velocity.runtime.resource.loader.StringResourceRepositoryImpl");
            velocityProperties.setProperty("string.resource.loader.repository.static", "false");
            velocityProperties.setProperty("string.resource.loader.repository.name", "JUnitGenerator");

            //create the velocity engine with an externalized resource template
            final VelocityEngine ve = new VelocityEngine(velocityProperties);
            //set our custom log adapter
            ve.setProperty("runtime.log.logsystem", new LogAdapter());
            //manage the repository and put our template in with a name
            StringResourceRepository repository = new StringResourceRepositoryImpl();
            repository.putStringResource(VIRTUAL_TEMPLATE_NAME, JUnitGeneratorSettings.getInstance(genCtx.getProject()).getVmTemplate());
            ve.setApplicationAttribute("JUnitGenerator", repository);

            //init the engine
            ve.init();

            final VelocityContext context = new VelocityContext();
            context.put("entryList", entryList);
            context.put("today", JUnitGeneratorUtil.formatDate("MM/dd/yyyy"));
            context.put("date", new DateTool());

            final Template template = ve.getTemplate(VIRTUAL_TEMPLATE_NAME);
            final StringWriter writer = new StringWriter();

            template.merge(context, writer);
            String outputFileName = (String) context.get("testClass");
            if (outputFileName == null || outputFileName.trim().length() == 0) {
                if (entryList != null && entryList.size() > 0) {
                    outputFileName = entryList.get(0).getClassName() + "Test";
                } else {
                    outputFileName = "UnknownTestCaseNameTest";
                }
            }
            ApplicationManager.getApplication()
                    .runWriteAction(
                            new JUnitGeneratorFileCreator(
                                    JUnitGeneratorUtil.resolveOutputFileName(genCtx, outputFileName),
                                    writer, genCtx));
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
