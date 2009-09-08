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
import org.intellij.plugins.junitgen.util.GenUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * This is where the magic happens.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 3, 2003</pre>
 * @updated By: Bryan Gilbert, July 18, 2008
 */
public class JUnitGeneratorActionHandler extends EditorWriteActionHandler {
    private static Logger logger = GenUtil.getLogger(JUnitGeneratorActionHandler.class.getName());
    private List entryList;
    private GeneratorContext genCtx;

    private Boolean generateOverload;
    private String overloadType;
    private Boolean combineGetterAndSetter;

    /**
     * Executed upon action in the Editor
     *
     * @param editor      IDEA Editor
     * @param dataContext DataCOntext
     */
    public void executeWriteAction(Editor editor, DataContext dataContext) {
        PsiJavaFile file = GenUtil.getSelectedJavaFile(dataContext);

        if (file == null) {
            return;
        }

        PsiClass[] psiClasses = file.getClasses();

        if (psiClasses == null) {
            return;
        }

        try {
            generateOverload = GenUtil.getGenerateOverload();
            overloadType = GenUtil.getGenerateOverloadType();
            combineGetterAndSetter = GenUtil.getCombineGetterSetter();
        } catch(IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < psiClasses.length; i++) {
            if ((psiClasses[i] != null) && (psiClasses[i].getQualifiedName() != null)) {
                genCtx = new GeneratorContext(dataContext, file, psiClasses[i]);
                entryList = new ArrayList();

                try {
                    if (psiClasses[i] == null) {
                        return;
                    }

                    if (!psiClasses[i].isInterface()) {
                        boolean getPrivate = true;

                        List methodList = new ArrayList();
                        List pMethodList = new ArrayList();
                        List fieldList = new ArrayList();

                        List<MethodComposite> methodCompositeList = new ArrayList<MethodComposite>();
                        List<MethodComposite> privateMethodCompositeList = new ArrayList<MethodComposite>();

                        buildMethodList(psiClasses[i].getMethods(), methodList, !getPrivate);
                        buildMethodList(psiClasses[i].getMethods(), pMethodList, getPrivate);
                        buildFieldList(psiClasses[i].getFields(), fieldList);
                        PsiClass[] innerClass = psiClasses[i].getAllInnerClasses();

                        for (int idx = 0; idx < innerClass.length; idx++) {
                            buildMethodList(innerClass[idx].getMethods(), methodList, !getPrivate);
                            buildMethodList(innerClass[idx].getMethods(), pMethodList, getPrivate);
                            buildFieldList(psiClasses[i].getFields(), fieldList);
                        }

                        processMethods(methodList, methodCompositeList);
                        processMethods(pMethodList, privateMethodCompositeList);

                        entryList.add(new TemplateEntry(genCtx.getClassName(false),
                                                        genCtx.getPackageName(),
                                                        methodCompositeList,
                                                        privateMethodCompositeList,
                                                        fieldList));
                        process();
                    }
                }
                catch (Exception e) {
                    GenUtil.getLogger(getClass().getName()).error(e);
                }
            }
        }
    }


    /**
     * Creates a list of methods with set and get methods combined together.
     *
     * @param methodList list of methods to process
     * @return a list of methods with set and get methods combined together. 
     */
    private void processMethods(List methodList, List<MethodComposite> methodCompositeList) {
        List methodNames = new ArrayList();
        List<MethodComposite> methodComposites;

        methodComposites = convertToComposites(methodList);

        if(generateOverload) {
            methodComposites = updateOverloadedMethods(methodComposites);
        }

        for(MethodComposite method : methodComposites) {
            String methodName = method.getName();

            if(methodName.startsWith("set") || methodName.startsWith("get") || methodName.startsWith("is") && combineGetterAndSetter) {
                methodName = parseAccessorMutator(methodName, methodList);
            }

            if(!methodNames.contains(methodName)) {
                methodNames.add(methodName);
                method.setName(methodName);
                methodCompositeList.add(method);
            }
        }
    }

    /**
     * Create a MethodComposite object for each of the methods passed in
     * @param methodList
     * @return
     */
    private List<MethodComposite> convertToComposites(List<PsiMethod> methodList) {

        List<MethodComposite> compositeList = new ArrayList<MethodComposite>();

        for(PsiMethod method : methodList) {

            List<String> paramClassList = new ArrayList<String>();
            for(PsiParameter param : method.getParameterList().getParameters()) {
                String className = (new StringTokenizer(param.getText(), " ")).nextToken();
                paramClassList.add(className);
            }

            List<String> paramNameList = new ArrayList<String>();
            for(PsiParameter param : method.getParameterList().getParameters()) {
                paramNameList.add(param.getName());
            }

            String signature = createSignature(method);

            List<String> reflectionCode = createReflectionCode(method);

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

        for(PsiParameter param : method.getParameterList().getParameters()) {
            params += param.getText() + ", ";
        }

        if(params.endsWith(", ")) {
            params = params.substring(0, params.length()-2);
        }

        signature = method.getName() + "(" + params + ")";

        return signature;

    }

    private List<String> createReflectionCode(PsiMethod method) {

        String getMethodText = "\"" + method.getName() + "\"";

        for(PsiParameter param : method.getParameterList().getParameters()) {
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

    private List<MethodComposite> updateOverloadedMethods(List<MethodComposite> methodList) {

        HashMap<String, Integer> methodNameMap = new HashMap<String, Integer>();
        HashMap<String, Integer> overloadMethodNameMap = new HashMap<String, Integer>();

        for(MethodComposite method : methodList) {
            String methodName = method.getName();
            if(!methodNameMap.containsKey(methodName)) {
                methodNameMap.put(methodName, 1);
            } else {
                Integer count = methodNameMap.get(methodName);
                methodNameMap.remove(methodName);
                methodNameMap.put(methodName, count+1);
            }
        }

        for(String key : methodNameMap.keySet()) {
            if(methodNameMap.get(key) > 1) {
                overloadMethodNameMap.put(key, methodNameMap.get(key));
            }
        }
        
        for(int i = 0; i < methodList.size(); i++) {

            MethodComposite method = methodList.get(i);
            String methodName = method.getName();
            if(overloadMethodNameMap.containsKey(methodName)) {
                int count = overloadMethodNameMap.get(methodName);
                overloadMethodNameMap.remove(methodName);
                overloadMethodNameMap.put(methodName, count-1);
                methodList.set(i, mutateOverloadedMethodName(method, count));
            }
        }

        return methodList;
    }

    private MethodComposite mutateOverloadedMethodName(MethodComposite method, int count) {

        String stringToAppend = "";

        if(GenUtil.NUMBER.equalsIgnoreCase(overloadType)) {
            stringToAppend += count;
        } else if(GenUtil.PARAM_CLASS.equalsIgnoreCase(overloadType)) {

            if(method.getParamClasses().size() > 1) {
                stringToAppend += "For";    
            }

            for(String paramClass : method.getParamClasses()) {
                paramClass = paramClass.substring(0,1).toUpperCase() + paramClass.substring(1,paramClass.length());
                stringToAppend += paramClass;
            }
        } else if(GenUtil.PARAM_NAME.equalsIgnoreCase(overloadType)) {

            if(method.getParamNames().size() > 1) {
                stringToAppend += "For";
            }

            for(String paramName : method.getParamNames()) {
                paramName = paramName.substring(0,1).toUpperCase() + paramName.substring(1,paramName.length());
                stringToAppend += paramName;
            }
        }

        method.setName(method.getName() + stringToAppend);

        return method;
    }

    /**
     * This method takes in an accessor or mutator method that is named using get*, set*, or is* and combines
     * the method name to provide one method name: "GetSet<BaseName>"
     * @param methodName - Name of accessor or mutator method
     * @param methodList - Entire list of method using to create test
     * @return String updated method name if list contains both accessor and modifier for base name
     */
    private String parseAccessorMutator(String methodName, List methodList) {

        String baseName;

        if(methodName.startsWith("is")) {
            baseName = methodName.substring(2);
        } else {
            baseName = methodName.substring(3);
        }

        if(methodList.contains("get"+ baseName) && (methodList.contains("set"+ baseName) || methodList.contains("is" + baseName))) {
            methodName = "GetSet" + baseName;
        }

        return methodName;
    }

    /**
     * Builds a list of class scope fields from an array of PsiFields
     * @param fields an array of fields
     * @param fieldList list to be populated
     */
    private void buildFieldList(PsiField[] fields, List fieldList) {
        for(int i = 0; i < fields.length; i++){
            fieldList.add(fields[i].getName());
        }
    }

    /**
     * Builds method List from an array of PsiMethods
     *
     * @param methods    array of methods
     * @param methodList list to be populated
     * @param getPrivate boolean value, if true returns only private methods, if false only returns none private methods
     */
    private void buildMethodList(PsiMethod[] methods, List methodList, boolean getPrivate) {

        for (int j = 0; j < methods.length; j++) {
            if (!methods[j].isConstructor()) {
                PsiModifierList modifiers = methods[j].getModifierList();

                if ((!modifiers.hasModifierProperty("private") && !getPrivate) || (modifiers.hasModifierProperty("private") && getPrivate)) {
                    methodList.add(methods[j]);
                }
            }
        }
    }

    /**
     * Sets all the needed vars in VelocityContext and
     * merges the template
     */
    private void process() {
        try {
            Properties velocityProperties = new Properties();
            velocityProperties.setProperty(VelocityEngine.RESOURCE_LOADER, Const.RESOURCE_LOADER_TYPE);
            velocityProperties.setProperty(Const.RESOURCE_LOADER_CLASS_KEY, Const.RESOURCE_LOADER_CLASS_VALUE);
            velocityProperties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
                    GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME));

            VelocityContext context = new VelocityContext();
            context.put(Const.ENTRY_LIST_VAR_NAME, entryList);
            context.put(Const.TODAY_VAR_NAME, GenUtil.formatDate("MM/dd/yyyy"));

            VelocityEngine ve = new VelocityEngine();
            ve.init(velocityProperties);

            Template template = ve.getTemplate(Const.TEMPLATE_NAME);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            genCtx.setOutputFileName((String) context.get(Const.CLASS_NAME_VAR));
            ApplicationManager.getApplication().runWriteAction(new FileCreator(GenUtil.getOutputFile(genCtx,
                    genCtx.getOutputFileName()), writer, genCtx));
        }
        catch (Exception e) {
            logger.error(e);
        }
    }

    public class MethodComposite {

        private PsiMethod method;
        private String name;
        private String signature;
        private List<String> paramClasses;
        private List<String> paramNames;
        private List<String> reflectionCode;

        public PsiMethod getMethod() {
            return method;
        }

        public void setMethod(PsiMethod method) {
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public List<String> getParamClasses() {
            return paramClasses;
        }

        public void setParamClasses(List<String> paramClasses) {
            this.paramClasses = paramClasses;
        }

        public List<String> getParamNames() {
            return paramNames;
        }

        public void setParamNames(List<String> paramNames) {
            this.paramNames = paramNames;
        }

        public List<String> getReflectionCode() {
            return reflectionCode;
        }

        public void setReflectionCode(List<String> reflectionCode) {
            this.reflectionCode = reflectionCode;
        }
    }

    /**
     * DataHolder class. Needs to be public since velocity is using it in the
     * template.
     */
    public class TemplateEntry {
        private List<MethodComposite> methodList;
        private List<MethodComposite> privateMethodList;
        private List fieldList = new ArrayList();

        private String className;
        private String packageName;


        public TemplateEntry(String className, String packageName, List<MethodComposite> methodList, List<MethodComposite> privateMethodList, List fieldList) {
            this.className = className;
            this.packageName = packageName;
            this.methodList = methodList;
            this.privateMethodList = privateMethodList;
            this.fieldList = fieldList;
        }

        public String getClassName() {
            return className;
        }

        public String getPackageName() {
            return packageName;
        }

        public List getFieldList() {
            return fieldList;
        }

        public List<MethodComposite> getMethodList() {
            return methodList;
        }

        public List<MethodComposite> getPrivateMethodList() {
            return privateMethodList;
        }
    }
}
