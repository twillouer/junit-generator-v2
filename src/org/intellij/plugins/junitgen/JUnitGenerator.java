package org.intellij.plugins.junitgen;

import com.intellij.openapi.components.ApplicationComponent;


/**
 * ApplicationComponent implementation
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 29, 2003</pre>
 */
public class JUnitGenerator implements ApplicationComponent {
    /**
     * Method is called after plugin is already created and configured. Plugin can start to communicate with
     * other plugins only in this method.
     */
    public void initComponent() {}

    /**
     * This method is called on plugin disposal.
     */
    public void disposeComponent() {}

    /**
     * Returns the name of component
     * @return String representing component name. Use plugin_name.component_name notation.
     */
    public String getComponentName() {
        return "JUnitGenerator.JUnitGenerator";
    }
}
