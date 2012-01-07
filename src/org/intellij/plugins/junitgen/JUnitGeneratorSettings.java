package org.intellij.plugins.junitgen;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;

/**
 * This is the settings wrapper. We persist settings to disk in various ways from
 * this class.
 *
 * @author JOsborne
 * @since 1/4/12 2:56 PM
 */
public class JUnitGeneratorSettings implements PersistentStateComponent<JUnitGeneratorSettings> {

    private static final Logger log = Logger.getInstance(JUnitGeneratorSettings.class);

    @State(
            name = "JUnitGeneratorSettings",
            storages = {
                    @Storage(id = "app-default", file = "$APP_CONFIG$/junitgenerator-settings.xml")})
    public static class App extends JUnitGeneratorSettings {
        public App() {
            loadDefaultState(this);
        }
    }

    @State(
            name = "JUnitGeneratorSettings",
            storages = {
                    @Storage(id = "prj-default", file = "$PROJECT_FILE$"),
                    @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/junitgenerator-settings.xml", scheme = StorageScheme.DIRECTORY_BASED)})
    public static class Prj extends JUnitGeneratorSettings {
        public Prj() {
            //load the default state from the application state
            loadDefaultState(this);
        }
    }

    /**
     * Return an instance from the service manager
     *
     * @return the settings instance
     */
    public static JUnitGeneratorSettings getInstance() {
        return ServiceManager.getService(JUnitGeneratorSettings.class);
    }

    /**
     * Return the proper settings based on the configuration of the project. If the project settings
     * are not used, this method returns the global settings instance
     *
     * @param project the project
     * @return the settings instance
     */
    public static JUnitGeneratorSettings getInstance(Project project) {
        JUnitGeneratorSettings settings = getProjectInstance(project);
        if (!settings.isUseProjectSettings()) {
            if (log.isDebugEnabled()) {
                log.debug("Project is configured for global settings, so we return the global settings");
            }
            settings = getInstance();
        }
        return settings;
    }

    /**
     * Return the project settings, regardless of the configuration
     *
     * @param project the project
     * @return the settings instance
     */
    public static JUnitGeneratorSettings getProjectInstance(Project project) {
        return ServiceManager.getService(project, JUnitGeneratorSettings.class);
    }

    @Override
    public JUnitGeneratorSettings getState() {
        return this;
    }

    @Override
    public void loadState(JUnitGeneratorSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

    /**
     * Load the default state
     *
     * @param settings the settings to load into
     */
    public static void loadDefaultState(JUnitGeneratorSettings settings) {
        log.debug("setting up default configuration");
        settings.setOutputFilePattern(JUnitGeneratorUtil.getProperty("junit.generator.outputPath"));
        settings.setGenerateForOverloadedMethods(true);
        settings.setListOverloadedMethodsBy("paramName");
        settings.setCombineGetterAndSetter(false);
        settings.setVmTemplate(JUnitGeneratorUtil.getProperty("junit.generator.vm.default"));
        settings.setUseProjectSettings(false);
    }

    private String outputFilePattern;
    private boolean generateForOverloadedMethods;
    private String listOverloadedMethodsBy;
    private boolean combineGetterAndSetter;
    private String vmTemplate;
    private boolean useProjectSettings;

    public String getOutputFilePattern() {
        return outputFilePattern;
    }

    public void setOutputFilePattern(String outputFilePattern) {
        this.outputFilePattern = outputFilePattern;
    }

    public String getListOverloadedMethodsBy() {
        return listOverloadedMethodsBy;
    }

    public void setListOverloadedMethodsBy(String listOverloadedMethodsBy) {
        this.listOverloadedMethodsBy = listOverloadedMethodsBy;
    }

    public boolean isGenerateForOverloadedMethods() {
        return generateForOverloadedMethods;
    }

    public void setGenerateForOverloadedMethods(boolean generateForOverloadedMethods) {
        this.generateForOverloadedMethods = generateForOverloadedMethods;
    }

    public boolean isCombineGetterAndSetter() {
        return combineGetterAndSetter;
    }

    public void setCombineGetterAndSetter(boolean combineGetterAndSetter) {
        this.combineGetterAndSetter = combineGetterAndSetter;
    }

    public String getVmTemplate() {
        return vmTemplate;
    }

    public void setVmTemplate(String vmTemplate) {
        this.vmTemplate = vmTemplate;
    }

    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }

    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }
}
