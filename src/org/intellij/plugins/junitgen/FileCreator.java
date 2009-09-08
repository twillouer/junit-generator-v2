package org.intellij.plugins.junitgen;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiJavaFile;
import org.intellij.plugins.junitgen.diff.DiffFileAction;
import org.intellij.plugins.junitgen.util.GenUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Responsible for writing test case out to the file and bringing
 * new editor window up.
 * Must implement runnable since we are using Application.runWriteAction in
 * JUnitGeneratorAction to refresh the content of the VirtualFileSystem.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 1, 2003</pre>
 */
public class FileCreator implements Runnable {
    private String outputFile;
    private StringWriter writer;
    private DataContext ctx;
    private PsiJavaFile file;
    private GeneratorContext genCtx;

    /**
     * Default constructor
     * @param outputFile output file name
     * @param writer holds the content of the file
     * @param genCtx generator context
     */
    public FileCreator(String outputFile, StringWriter writer, GeneratorContext genCtx) {
        this.outputFile = outputFile + ".java";
        this.writer = writer;
        this.ctx = genCtx.getDataContext();
        this.file = genCtx.getFile();
        this.genCtx = genCtx;
    }

     /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public void run() {
        File newFile = new File(outputFile);
        int overwriteInd = JOptionPane.NO_OPTION;

        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        if (newFile.exists()) {
            overwriteInd =
                JOptionPane.showOptionDialog(null, Const.OVERWRITE_MSG, "View the difference?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        }

        if (JOptionPane.NO_OPTION == overwriteInd) {
            try {
                FileWriter w = new FileWriter(newFile);
                w.write(writer.toString());
                w.flush();
                w.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            //Refresh and open new file in the editor
            Project project = GenUtil.getProject(ctx);
            VirtualFileSystem vfs = file.getVirtualFile().getFileSystem();

            GenUtil.getLogger(getClass().getName()).info("OutputFile: " + outputFile);
            VirtualFile fileToOpen = vfs.refreshAndFindFileByPath(outputFile);

            if(fileToOpen != null) {
                FileEditorManager.getInstance(project).openFile(fileToOpen, true);
            }
            else {
                throw new IllegalArgumentException("Unable to find: " + outputFile);
            }
        }

        else if (JOptionPane.YES_OPTION == overwriteInd) {
            VirtualFileSystem vfs = file.getVirtualFile().getFileSystem();
            new DiffFileAction().showDiff(writer.toString(), vfs.findFileByPath(outputFile), genCtx);
        }
    }
}
