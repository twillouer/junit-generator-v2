package org.intellij.plugins.junitgen.diff;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.junitgen.GeneratorContext;

import java.io.IOException;


public final class DiffFileAction {
    public void showDiff(String paneTwoContents, VirtualFile paneTwoFile, GeneratorContext genCtx) {
        Project project =
            (Project) genCtx.getDataContext().getData(com.intellij.openapi.actionSystem.DataConstants.PROJECT);

        if (project != null) {
            if ((paneTwoFile != null) && (paneTwoContents != null)) {
                DiffFileToken paneOneToken = null;

                try {
                    paneOneToken = DiffFileToken.createForVirtualFile(paneTwoFile);
                }
                catch (IOException e) {
                    System.err.println(new StringBuffer("DiffFile plugin: could not read selected file: ").append(
                            paneTwoFile.getPath()).toString());
                    e.printStackTrace();

                    Messages.showDialog(project, "Could not read contents of selected file.", // message
                        "Diff File Error", // title
                        new String[] { "Dismiss" }, 0, Messages.getErrorIcon());

                    return;
                }

                if (paneOneToken != null) {
                    DiffFileToken paneTwoToken = new DiffFileToken();

                    paneTwoToken.setName("Editor Contents");
                    paneTwoToken.setTitle("Temporary Buffer");
                    paneTwoToken.setContents(paneTwoContents);
                    paneTwoToken.setType(FileTypeManager.getInstance().getFileTypeByExtension(paneTwoFile.getExtension()));

                    DiffViewerFrame diffViewer =
                        new DiffViewerFrame(project, paneTwoFile, paneOneToken, paneTwoToken, genCtx);

                    diffViewer.setVisible(true);
                }
            }
        }
    }
}
