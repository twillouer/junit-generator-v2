package org.intellij.plugins.junitgen.util;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.junitgen.GeneratorContext;
import org.intellij.plugins.junitgen.JUnitGeneratorSettings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


/**
 * General purpose utility class.
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @since <pre>Aug 30, 2003</pre>
 */
public class JUnitGeneratorUtil {

    private static final Logger log = getLogger(JUnitGeneratorUtil.class);

    public static final String NUMBER = "number";
    public static final String PARAM_NAME = "paramName";
    public static final String PARAM_CLASS = "paramClass";

    public static final Pattern SOURCE_PATH_PATTERN = Pattern.compile("\\$\\{SOURCEPATH\\}");
    public static final Pattern PACKAGE_PATTERN = Pattern.compile("\\$\\{PACKAGE\\}");
    public static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("\\.");
    public static final Pattern FILENAME_PATTERN = Pattern.compile("\\$\\{FILENAME\\}");
    public static final String PATH_PATTERN = new StringBuilder("\\").append(File.separator).toString();

    /**
     * The icon is static so we load it here and once
     */
    public static final Icon ICON = IconLoader.getIcon("/org/intellij/plugins/junitgen/smalllogo.gif");


    /**
     * Return a resource from the property bundle
     *
     * @param key the key
     * @return the value
     */
    public static String getProperty(String key) {
        return ResourceBundle.getBundle("vm-template").getString(key);
    }

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
     * Gets the javafile that's currently selected in the editor. Returns null if it's not a javafile.
     *
     * @param dataContext data context.
     * @return The current javafile. Null if not a javafile.
     */
    public static PsiJavaFile getSelectedJavaFile(DataContext dataContext) {
        final PsiFile psiFile = (PsiFile) dataContext.getData("psi.File");

        if (!(psiFile instanceof PsiJavaFile)) {
            return null;
        } else {
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
        return DataKeys.PROJECT.getData(ctx);
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
     * Returns a logger
     *
     * @param clazz class name for which logger should be constructed
     * @return a logger instance
     */
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Returns source paths for currently selected java file
     *
     * @param clss psiClass
     * @param ctx  the data context
     * @return list of source paths
     */
    public static String getSourcePath(PsiClass clss, DataContext ctx) {
        if (clss == null) {
            return null;
        } else if (clss.getContainingFile() == null) {
            return null;
        } else if (clss.getContainingFile().getVirtualFile() == null) {
            return null;
        }

        VirtualFile[] roots = ProjectRootManager.getInstance(getProject(ctx)).getContentSourceRoots();
        String className = clss.getContainingFile().getVirtualFile().getPath();

        if (className != null) {
            for (VirtualFile root : roots) {
                if (className.startsWith(root.getPath())) {
                    return root.getPath();
                }
            }
        }
        return null;
    }

    /**
     * Returns absolute path + filename of the output file
     *
     * @param genCtx        GeneratorContext
     * @param testClassName the class name of the test we are creating
     * @return absolute path + filename of the output file
     * @throws java.io.IOException when there are problems
     */
    public static String resolveOutputFileName(GeneratorContext genCtx, String testClassName)
            throws IOException {
        String outputPattern = JUnitGeneratorSettings.getInstance(genCtx.getProject()).getOutputFilePattern();
        String sourcePath = getSourcePath(genCtx.getPsiClass(), genCtx.getDataContext());

        if (sourcePath == null) {
            throw new IllegalArgumentException("file source path cannot be null");
        }

        String packageName = genCtx.getFile().getPackageName();

        int indexOf = sourcePath.indexOf("$SOURCEPATH$");
        //trim the leading part of the string because it MUST start with the path
        if (indexOf != -1) {
            sourcePath = sourcePath.substring(indexOf);
        }

        //replace our tokens with regular expressions
        outputPattern = SOURCE_PATH_PATTERN.matcher(outputPattern).replaceAll(sourcePath);
        outputPattern = PACKAGE_PATTERN.matcher(outputPattern).replaceAll(
                PACKAGE_NAME_PATTERN.matcher(packageName).replaceAll(PATH_PATTERN));
        outputPattern = FILENAME_PATTERN.matcher(outputPattern).replaceAll(testClassName);
        outputPattern += '.' + genCtx.getFile().getFileType().getDefaultExtension();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Returning output pattern %s", outputPattern));
        }
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

    /**
     * Read file contents in a 'safe' way using {@link Computable}
     *
     * @param file the file to read
     * @return null if the file is null, otherwise, the contents encoded with the file's charset
     */
    public static String readFileContents(final VirtualFile file) {
        return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
            @Override
            public String compute() {
                try {
                    return new String(file.contentsToByteArray(), file.getCharset().name());
                } catch (IOException e) {
                    log.warn(String.format("could not read file %s", file), e);
                }
                return null;
            }
        });
    }
}
