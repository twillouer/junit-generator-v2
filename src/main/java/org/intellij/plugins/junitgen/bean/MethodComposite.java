package org.intellij.plugins.junitgen.bean;

import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * A holder for the dissection of the methods
 *
 * @author Jon Osborn
 * @since 1/3/12 4:37 PM
 */
public class MethodComposite {

    private PsiMethod method;
    private String name;
    private String signature;
    private List<String> paramClasses;
    private List<String> paramNames;
    private List<String> reflectionCode;
    private MethodComposite base;
    private List<MethodComposite> overloadedMethods = new ArrayList<MethodComposite>();

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

    public MethodComposite getBase() {
        return base;
    }

    public void setBase(MethodComposite base) {
        this.base = base;
    }

    public List<MethodComposite> getOverloadedMethods() {
        return overloadedMethods;
    }

    public void setOverloadedMethods(List<MethodComposite> overloadedMethods) {
        this.overloadedMethods = overloadedMethods;
    }

    @Override
    public String toString() {
        return "MethodComposite{" +
                "base=" + base +
                ", method=" + method +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                ", paramClasses=" + paramClasses +
                ", paramNames=" + paramNames +
                ", reflectionCode=" + reflectionCode +
                ", overloadedMethods=" + overloadedMethods +
                '}';
    }
}
