package org.intellij.plugins.junitgen;

import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;
import org.junit.Test;

import java.io.File;
import java.util.regex.Matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test some of the conditions in the utility
 *
 * @author Jon Osborn
 * @since <pre>1/7/12 1:12 PM</pre>
 */
public class JUnitGeneratorUtilTest {


    @Test
    public void testSourcePathExpression() {
        Matcher matcher = JUnitGeneratorUtil.SOURCE_PATH_PATTERN.matcher("${SOURCEPATH}/test/my/path");
        assertTrue(matcher.find());
        assertTrue(matcher.regionStart() == 0);
    }

    @Test
    public void testSourcePathExpression_1() {
        Matcher matcher = JUnitGeneratorUtil.SOURCE_PATH_PATTERN.matcher("   ${SOURCEPATH}/test/my/path");
        assertTrue(matcher.find());
        assertFalse(matcher.start() == 0);
        assertTrue(matcher.start() == 3);
    }

    @Test
    public void testReplacementExpression() {
        String replacement = "\\";
        String path = "my.test.package.name.to.file.path";

        String result = path.replace(".", File.separator);
        System.out.println(result);
        System.out.println(path.replace(".", "/"));
        //result = result.replaceAll("\\\\\\\\","\\\\");
        //System.out.println(result);

        System.out.println(JUnitGeneratorUtil.PACKAGE_NAME_PATTERN.matcher(path).replaceAll("\\\\"));
        //String result = path.replaceAll("\\.", "\\\\");//   matcher.replaceAll(replacement);
        //result = result.replace("\\\\", "\\");
        //result = result.replaceAll("\\\\", "\\");//   matcher.replaceAll(replacement);
        //assertTrue(result.contains(File.separator));

    }
}
