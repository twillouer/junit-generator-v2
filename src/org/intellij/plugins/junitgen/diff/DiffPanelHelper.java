package org.intellij.plugins.junitgen.diff;

import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffPanel;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;

import java.awt.*;


/**
 * Class to add some convenience abilities to the DiffPanel.
 */
public final class DiffPanelHelper {
    private Project project = null;
    private DiffFileToken paneTwoToken = null;
    private DiffFileToken paneOneToken = null;
    private DiffPanel panel = null;
    private Window window = null;

    /**
     * Create a new DiffPanelHelper for the specified window, project, backing file, and contents.
     * The virtual file is used when a line number is selected.
     *
     * @param window       The window that will contain the diffPanel.
     * @param project      The project this window is associated with.
     * @param paneOneToken A token containing information about the file to be placed in the left
     *                     hand pane.
     * @param paneTwoToken A token containing information about the file to be placed in the right
     */
    public DiffPanelHelper(Window window, Project project, DiffFileToken paneOneToken, DiffFileToken paneTwoToken) {
        this.window = window;
        this.project = project;
        this.paneTwoToken = paneTwoToken;
        this.paneOneToken = paneOneToken;
    }

    public String getTitle() {
        return new StringBuffer(this.paneOneToken.getName()).
                append(" vs. ").append(this.paneTwoToken.getName()) .toString();
    }

    public DiffPanel getDiffPanel() {
        this.initDiffPanel();

        return this.panel;
    }

    public void closeDiffPanel() {
        if (this.panel != null) {
            this.panel.dispose();
            this.panel = null;
        }
    }

    private void initDiffPanel() {
        if (this.panel == null) {
            this.panel = DiffManager.getInstance().createDiffPanel(this.window, this.project);

            FileType fileType = paneTwoToken.getType();

            if (fileType == null) {
                fileType = paneOneToken.getType();
            }

            this.panel.setTitle1(this.paneOneToken.getTitle());
            this.panel.setTitle2(this.paneTwoToken.getTitle());
            this.panel.setContents(new SimpleContent(this.paneOneToken.getContents(), fileType),
                    new SimpleContent(this.paneTwoToken.getContents(), fileType));
        }
    }

}
