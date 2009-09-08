package org.intellij.plugins.junitgen;


/**
 * Constants
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 30, 2003</pre>
 */
public class Const {
    public static final String RELATIVE_DIR_NAME = "resources";
    public static final String ENTRY_LIST_VAR_NAME = "entryList";
    public static final String TODAY_VAR_NAME = "today";
    public static final String TEMPLATE_NAME = "junitgen.vm";
    public static final String RESOURCE_LOADER_TYPE = "file";
    public static final String RESOURCE_LOADER_CLASS_KEY = "file.resource.loader.class";
    public static final String RESOURCE_LOADER_CLASS_VALUE = "org.apache.velocity.runtime.resource.loader.FileResourceLoader";
    public static final String PROPERTIES_FILE_NAME = "junitgen.properties";
    public static final String OUTPUT_KEY = "output";
    public static final String GENERATE_OVERLOADED_METHODS_KEY = "generateForOverloadedMethods";
    public static final String GENERATE_OVERLOAD_TYPE_KEY = "listOverloadedMethodsBy";
    public static final String COMBINE_GETTER_SETTER_KEY = "combineGetterAndSetter";
    public static final String OVERWRITE_MSG =
        "File already exists. Do you want to see the difference between the old and the new test cases?\n"
        + "By answering 'No' you will overwrite an existing file.";
    public static final String CLASS_NAME_VAR = "testClass";
    public static final String DATE_FORMAT_VAR = "dateFormat";

    private Const() {}
}
