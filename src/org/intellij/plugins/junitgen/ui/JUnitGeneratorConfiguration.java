package org.intellij.plugins.junitgen.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import org.intellij.plugins.junitgen.JUnitGeneratorSettings;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author JOsborne
 * @since 1/4/12 2:44 PM
 */
public class JUnitGeneratorConfiguration {
    private JPanel pane;
    private JTabbedPane tabbedPane1;
    private JCheckBox combineGetterAndSetterCheckBox;
    private JCheckBox generateForOverloadedMethodsCheckBox;
    private JComboBox methodGenerationComboBox;
    private JTextField outputTextBox;
    private JComboBox settingsTypeComboBox;
    private JLabel settingsTypeLabel;
    private JButton copyGobalSettingsToButton;
    private Editor velocityEditor;
    private final Project project;

    public JUnitGeneratorConfiguration(JUnitGeneratorSettings settings, Project project) {
        this.project = project;
        setSettings(settings);
        updateSettingsVisibility();
        this.settingsTypeComboBox.setVisible(this.project != null);
        this.settingsTypeLabel.setVisible(this.project != null);
        //if we use global settings, then hide the project settings
        this.settingsTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateSettingsVisibility();
            }
        });
        copyGobalSettingsToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("copy".equals(e.getActionCommand())) {
                    copyGlobalSettings();
                }
            }
        });
    }

    public JPanel getPane() {
        return pane;
    }

    public void setSettings(JUnitGeneratorSettings settings) {
        setGenerateForOverloadedMethods(settings.isGenerateForOverloadedMethods());
        setCombineGetterAndSetter(settings.isCombineGetterAndSetter());
        setListOverloadedMethodsBy(settings.getListOverloadedMethodsBy());
        setOutput(settings.getOutputFilePattern());
        setVmTemplate(settings.getVmTemplate());
        setUseProjectSettings(settings.isUseProjectSettings());
    }

    public boolean isUseProjectSettings() {
        return this.project != null && this.settingsTypeComboBox.getSelectedIndex() > 0;
    }

    public void setUseProjectSettings(boolean useProjectSettings) {
        this.settingsTypeComboBox.setSelectedIndex(useProjectSettings ? 1 : 0);
        this.tabbedPane1.setVisible(this.project == null || useProjectSettings);
    }

    public boolean isGenerateForOverloadedMethods() {
        return this.generateForOverloadedMethodsCheckBox.isSelected();
    }

    public void setGenerateForOverloadedMethods(boolean generateForOverloadedMethods) {
        this.generateForOverloadedMethodsCheckBox.setSelected(generateForOverloadedMethods);
    }

    public String getListOverloadedMethodsBy() {
        return this.methodGenerationComboBox.getSelectedItem().toString();
    }

    public void setListOverloadedMethodsBy(String listOverloadedMethodsBy) {
        for (int i = 0; i < this.methodGenerationComboBox.getItemCount(); i++) {
            if (this.methodGenerationComboBox.getItemAt(i).equals(listOverloadedMethodsBy)) {
                this.methodGenerationComboBox.setSelectedIndex(i);
                return;
            }
        }
        this.methodGenerationComboBox.setSelectedIndex(-1);
    }

    public boolean isCombineGetterAndSetter() {
        return this.combineGetterAndSetterCheckBox.isSelected();
    }

    public void setCombineGetterAndSetter(boolean combineGetterAndSetter) {
        this.combineGetterAndSetterCheckBox.setSelected(combineGetterAndSetter);
    }

    public String getOutput() {
        return this.outputTextBox.getText();
    }

    public void setOutput(String output) {
        this.outputTextBox.setText(output);
    }

    public String getVmTemplate() {
        return this.velocityEditor.getDocument().getText();
    }

    public void setVmTemplate(String text) {
        EditorFactory factory = EditorFactory.getInstance();
        if (this.velocityEditor != null) {
            this.tabbedPane1.remove(this.velocityEditor.getComponent());
        }
        final Document velocityTemplate = factory.createDocument(text != null ? text : "");
        this.velocityEditor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        this.tabbedPane1.addTab(JUnitGeneratorUtil.getProperty("junit.generator.ui.title.vm"), this.velocityEditor.getComponent());
    }

    public void updateSettingsVisibility() {
        this.tabbedPane1.setVisible(this.project == null || this.settingsTypeComboBox.getSelectedIndex() > 0);
        this.copyGobalSettingsToButton.setVisible(this.project != null);
    }

    /**
     * copy the global settings in but maintain the 'project settings' type
     */
    public void copyGlobalSettings() {
        boolean currentProjectSettingsType = isUseProjectSettings();
        setSettings(JUnitGeneratorSettings.getInstance());
        setUseProjectSettings(currentProjectSettingsType);
    }
}
