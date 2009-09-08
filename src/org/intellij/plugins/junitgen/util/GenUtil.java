package org.intellij.plugins.junitgen.util;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.junitgen.Const;
import org.intellij.plugins.junitgen.GeneratorContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;


/**
 * General purpose utility class.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 30, 2003</pre>
 */
public class GenUtil {

    public static final String NUMBER = "number";
    public static final String PARAM_NAME = "paramName";
    public static final String PARAM_CLASS = "paramClass";

    /**
     * Returns full path to the relativePath.
     *
     * @param relativePath directory relative to IDEA's home.
     * @return Full path to the relativePath passed in.
     */
    public static String getResourcePath(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("relative path should not be null");
        }
        String pluginPath = PathManager.getPluginsPath();
        pluginPath = pluginPath + File.separator + relativePath;

        return pluginPath;
    }

    /**
     * Get's the javafile that's currently selected in the editor. Returns null if it's not a javafile.
     *
     * @param dataContext data context.
     * @return The current javafile. Null if not a javafile.
     */
    public static PsiJavaFile getSelectedJavaFile(DataContext dataContext) {
        final PsiFile psiFile = (PsiFile) dataContext.getData("psi.File");

        if (!(psiFile instanceof PsiJavaFile)) {
            return null;
        }
        else {
            return (PsiJavaFile) psiFile;
        }
    }

    /**
     * Returns Project from DataContext
     *
     * @param ctx DataContext
     * @return Project object
     */
    public static Project getProject(DataContext ctx) {
        return (Project) ctx.getData(DataConstants.PROJECT);
    }

    /**
     * Returns a logger
     *
     * @param className class name for which logger should be constructed
     * @return a logger instance
     */
    public static Logger getLogger(String className) {
        return Logger.getInstance(className);
    }

    /**
     * Returns source paths for currently selected java file
     *
     * @param clss psiClass
     * @return list of source paths
     */
    public static String getSourcePath(PsiClass clss, DataContext ctx) {

        VirtualFile[] roots = ProjectRootManager.getInstance(getProject(ctx)).getContentSourceRoots();
        String className = clss.getContainingFile().getVirtualFile().getPath();

        for (int i = 0; i < roots.length; i++) {
            if (className.startsWith(roots[i].getPath())) {
                return roots[i].getPath();
            }
        }

        return null;
    }

    /**
     * Returns an output pattern
     *
     * @return output pattern
     * @throws java.io.IOException
     */
    public static String getOutputPattern(GeneratorContext genCtx)
            throws IOException {
        String resourcePath = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME);
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(resourcePath + "/" + Const.PROPERTIES_FILE_NAME));
        String outputPattern = configProps.getProperty(getProject(genCtx.getDataContext()).getName());

        if (outputPattern == null || outputPattern.trim().length() == 0) {
            outputPattern = configProps
                    .getProperty(Const.OUTPUT_KEY);
        }

        return outputPattern;
    }

    /**
     * Returns a boolean that determines whether or not to generate overloaded method test
     *
     * @return output pattern
     * @throws java.io.IOException
     */
    public static Boolean getGenerateOverload() throws IOException {
        String resourcePath = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME);
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(resourcePath + "/" + Const.PROPERTIES_FILE_NAME));

        Boolean generateOverload = !("false").equalsIgnoreCase(configProps.getProperty(Const.GENERATE_OVERLOADED_METHODS_KEY));

        return generateOverload;
    }

    /**
     * Returns a boolean that determines whether or not to generate overloaded method test
     *
     * @return output pattern
     * @throws java.io.IOException
     */
    public static String getGenerateOverloadType() throws IOException {
        String resourcePath = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME);
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(resourcePath + "/" + Const.PROPERTIES_FILE_NAME));

        String generateOverloadType = configProps.getProperty(Const.GENERATE_OVERLOAD_TYPE_KEY);

        if(NUMBER.equalsIgnoreCase(generateOverloadType)) {
            return NUMBER;
        } else if(PARAM_CLASS.equalsIgnoreCase(generateOverloadType)) {
            return PARAM_CLASS;
        } else {
            return PARAM_NAME;
        }
    }

    /**
     * Returns a boolean that determines whether or not to combine getter and setter tests
     *
     * @return output pattern
     * @throws java.io.IOException
     */
    public static Boolean getCombineGetterSetter() throws IOException {
        String resourcePath = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME);
        Properties configProps = new Properties();
        configProps.load(new FileInputStream(resourcePath + "/" + Const.PROPERTIES_FILE_NAME));

        Boolean combineGetterSetter = ("true").equalsIgnoreCase(configProps.getProperty(Const.COMBINE_GETTER_SETTER_KEY));

        return combineGetterSetter;
    }

    /**
     * Returns absolute path + filename of the output file
     *
     * @param genCtx GeneratorContext
     * @return absolute path + filename of the output file
     * @throws java.io.IOException
     */
    public static String getOutputFile(GeneratorContext genCtx, String testClassName)
            throws IOException {
        String outputPattern = getOutputPattern(genCtx);
        String sourcePath = getSourcePath(genCtx.getPsiClass(), genCtx.getDataContext());

        if (sourcePath == null) {
            throw new IllegalArgumentException("Sourcepath cannot be null");
        }

        String packageName = genCtx.getFile().getPackageName();

        int indexOf = sourcePath.indexOf("$SOURCEPATH$");

        if (indexOf != -1) {
            sourcePath = sourcePath.substring(indexOf);
        }

        outputPattern = StringUtil.replace(outputPattern, "$SOURCEPATH$", sourcePath);
        outputPattern = StringUtil.replace(outputPattern, "$PACKAGE$", packageName.replace('.', '/'));
        outputPattern = StringUtil.replace(outputPattern, "$FILENAME$", testClassName);

        return outputPattern;
    }

    /**
     * Returns Current date as formatted string, format specified by pattern argument.
     *
     * @param pattern date format
     * @return current date as formatted string, format specified by pattern argument.
     * @see SimpleDateFormat
     */
    public static String formatDate(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        return sdf.format(new java.util.Date());
    }
}
