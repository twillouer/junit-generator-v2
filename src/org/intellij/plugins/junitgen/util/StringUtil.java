package org.intellij.plugins.junitgen.util;


/**
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Aug 30, 2003</pre>
 */
public class StringUtil {
    /**
     * Replaces all occurrences of <code>replPattern</code> in the <code>inputStr</code>
     * with <code>replaceWith</code> string.
     * @param inputStr input string
     * @param replPattern replace pattern
     * @param replaceWith string to replace replPattern
     * @return inputStr with all occurrences of replPattern being replaced with relaceWith string.
     */
    public static String replace(String inputStr, String replPattern, String replaceWith) {
        while (true) {
            int indexOf = inputStr.indexOf(replPattern);

            if (indexOf == -1) {
                break;
            }

            int end = indexOf + replPattern.length();

            inputStr = inputStr.substring(0, indexOf) + replaceWith + inputStr.substring(end);
        }

        return inputStr;
    }

    /**
     * Capitalizes string passed in
     * @param strIn string to be capitalized
     * @return capitalized string
     * @throws java.lang.IndexOutOfBoundsException
     */
    public static String cap(String strIn) throws IndexOutOfBoundsException {
        if ((strIn == null) || (strIn.trim().length() == 0)) {
            throw new IllegalArgumentException("Argumnet strIn in method GenUtil.cap must not be null.");
        }

        return String.valueOf(strIn.charAt(0)).toUpperCase() + strIn.substring(1);
    }

    /**
     * Removes package name from classname
     * @param className class name
     * @return class name without package name
     */
    public static String trimPackageName(String className) {
        if (className == null) {
            return null;
        }

        int indexOf = className.lastIndexOf('.');

        if (indexOf != -1) {
            return className.substring(indexOf + 1);
        }
        else {
            return className;
        }
    }
}
