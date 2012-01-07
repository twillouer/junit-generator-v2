package org.intellij.plugins.junitgen.diff;

import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.junitgen.GeneratorContext;
import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;

import java.io.IOException;


/**
 * This is the class that shows a difference dialog when appropriate
 *
 * @author Jon Osborn
 */
public final class DiffFileAction {

    private static final Logger log = JUnitGeneratorUtil.getLogger(DiffFileAction.class);

    /**
     * Show the difference dialog
     *
     * @param proposedFileContents the new contents
     * @param existingFile         the new file
     * @param context              the context
     * @throws IOException IO Exception when there are issues
     */
    public void showDiff(String proposedFileContents, final VirtualFile existingFile, GeneratorContext context) throws IOException {

        final Project project = DataKeys.PROJECT.getData(context.getDataContext());

        if (project != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Showing diff for %s", existingFile.getPath()));
            }
            //grab the contents of the existing file using the read action
            final String existingFileContents = JUnitGeneratorUtil.readFileContents(existingFile);

            final MergeRequest request = DiffRequestFactory.getInstance()
                    .createMergeRequest(proposedFileContents,
                            existingFileContents,
                            existingFileContents,
                            existingFile,
                            project,
                            ActionButtonPresentation.APPLY,
                            ActionButtonPresentation.CANCEL_WITH_PROMPT);
            request.setWindowTitle("JUnit Merge");
            request.setVersionTitles(new String[]{"Generated Test", "Merge Result", "Existing Test"});
            request.setHelpId("help.junitgen.merge");
            final DiffTool tool = DiffManager.getInstance().getDiffTool();
            //make sure this request is processed on the proper thread
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    try {
                        tool.show(request);
                        log.debug("Showed Diff Tool");
                    } catch (Exception e) {
                        log.error("Exception while processing merge", e);
                    }
                }
            });
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Can't show diff when the project is null");
            }
        }
    }
}
