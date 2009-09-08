/*
 * Copyright (c) United Parcel Service of America, Inc.
 * All Rights Reserved.
 *                                                                                    
 * The use, disclosure, reproduction, modification, transfer, or transmittal          
 * of this work for any purpose in any form or by any means without the               
 * written permission of United Parcel Service is strictly prohibited.                
 * Confidential, Unpublished Property of United Parcel Service.                       
 * Use and Distribution Limited Solely to Authorized Personnel.                       
 */
package org.intellij.plugins.junitgen.ui;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.intellij.plugins.junitgen.Const;
import org.intellij.plugins.junitgen.util.GenUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * JUnitGenerator configuration UI.
 * Could be accessed via File->Settings->(IDE Settings) JUnit Generator
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 7, 2004</pre>
 */
public class JUnitGenConfig implements ApplicationComponent, Configurable {
    private Editor templateEditor;
    private Editor propsEditor;

    private String templateContent = "";
    private String propsContent = "";

    private final static String templateFile = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME) + File.separator + Const.TEMPLATE_NAME;
    private final static String propsFile = GenUtil.getResourcePath(Const.RELATIVE_DIR_NAME) + File.separator + Const.PROPERTIES_FILE_NAME;

    public String getDisplayName() {
        return "JUnit Generator";
    }

    public Icon getIcon() {
        return new ImageIcon(/*JUnitGenConfig.class.getResource("smalllogo.gif")*/);
    }

    public String getHelpTopic() {
        return null;
    }

    public String getComponentName() {
        return "JUnitGeneratorPlugin.JUnitGenConfig";
    }

    public void initComponent() {
        templateContent = readFile(templateFile);
        propsContent = readFile(propsFile);
    }

    public JComponent createComponent() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // test-case template configuration tab
        EditorFactory factory = EditorFactory.getInstance();
        char chars[] = new char[templateContent.length()];
        templateContent.getChars(0, templateContent.length(), chars, 0);

        templateEditor = factory.createEditor(factory.createDocument(chars));
        tabbedPane.addTab("Velocity Template", templateEditor.getComponent());

        templateEditor.getComponent().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), templateFile),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Output Properties configuration tab
        propsEditor = factory.createEditor(factory.createDocument(propsContent));
        tabbedPane.addTab("Output Pattern", propsEditor.getComponent());

        propsEditor.getComponent().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), propsFile),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return tabbedPane;
    }

    public boolean isModified() {
        return !templateContent.equals(templateEditor.getDocument().getText()) ||
                !propsContent.equals(propsEditor.getDocument().getText());
    }

    public void apply() throws ConfigurationException {
        templateContent = templateEditor.getDocument().getText();
        propsContent = propsEditor.getDocument().getText();
    }

    private String readFile(String filename) {
        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");
            byte bytes[] = new byte[(int) file.length()];
            file.readFully(bytes);
            file.close();
            return new String(bytes);
        }
        catch (Exception ex) {
            GenUtil.getLogger(getClass().getName()).error(ex);
        }
        throw new RuntimeException("Unable to read config files.");
    }

    private void writeFile(String filename, String content) {
        FileWriter file;
        try {
            file = new FileWriter(new File(filename), false);
            file.write(content);
            file.close();
        }
        catch (IOException e) {
            GenUtil.getLogger(getClass().getName()).error(e);
        }

    }

    public void disposeUIResources() {
        writeFile(templateFile, templateContent);
        writeFile(propsFile, propsContent);
    }

    public void disposeComponent() {
    }

    public void reset() {
    }

}
