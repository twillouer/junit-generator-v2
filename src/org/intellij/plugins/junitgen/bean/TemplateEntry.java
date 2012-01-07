package org.intellij.plugins.junitgen.bean;

import java.util.List;

/**
 * DataHolder class. Needs to be public since velocity is using it in the
 * template.
 *
 * @author Jon Osborn
 */
public class TemplateEntry {

    private final List<MethodComposite> methodList;
    private final List<MethodComposite> privateMethodList;
    private final List<String> fieldList;

    private String className;
    private String packageName;

    public TemplateEntry(String className,
                         String packageName,
                         List<MethodComposite> methodList,
                         List<MethodComposite> privateMethodList,
                         List<String> fieldList) {
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

    public List<String> getFieldList() {
        return fieldList;
    }

    public List<MethodComposite> getMethodList() {
        return methodList;
    }

    public List<MethodComposite> getPrivateMethodList() {
        return privateMethodList;
    }
}
