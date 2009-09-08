package org.intellij.plugins.junitgen.diff;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DimensionService;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.junitgen.FileCreator;
import org.intellij.plugins.junitgen.GeneratorContext;
import org.intellij.plugins.junitgen.util.GenUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringWriter;


public final class DiffViewerFrame extends JFrame implements DataProvider {
    private static final String DIMENSION_KEY = "DiffViewer.JFrame";
    private Project project = null;
    private DiffPanelHelper diffPanelHelper = null;
    private GeneratorContext ctx;
    private String content;

    public DiffViewerFrame(Project project, VirtualFile backingFile, DiffFileToken paneOneToken,
        DiffFileToken paneTwoToken, GeneratorContext ctx) {
        this.setProject(project);
        this.diffPanelHelper = new DiffPanelHelper(this, project, paneOneToken, paneTwoToken);

        this.ctx = ctx;
        this.content = paneTwoToken.getContents();
        this.setTitle(this.buildTitle());
        this.init();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Object getData(String dataId) {
        if (DataConstants.PROJECT.equals(dataId)) {
            return this.project;
        }
        else {
            return null;
        }
    }

    public void dispose() {
        this.diffPanelHelper.closeDiffPanel();
        super.dispose();
    }

    protected void init() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setIconImage(new ImageIcon(getClass().getResource("/diff/Diff.png")).getImage());

        this.getRootPane().registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    DimensionService dimensionService = DimensionService.getInstance();
                    dimensionService.setSize(DIMENSION_KEY, getSize());
                    dimensionService.setLocation(DIMENSION_KEY, getLocation());
                }
            });

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout(8, 0));
        contentPane.add(this.createCenterPanel(), BorderLayout.CENTER);
        contentPane.add(this.createButtonPanel(), BorderLayout.SOUTH);

        this.pack();

        DimensionService dimensionService = DimensionService.getInstance();
        Dimension size = dimensionService.getSize(DIMENSION_KEY);

        if (size != null) {
            this.setSize(size);
        }

        Point location = dimensionService.getLocation(DIMENSION_KEY);

        if (location != null) {
            this.setLocation(location);
        }
        else {
            this.setLocationRelativeTo(null);
        }
    }

    protected JComponent createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        Action[] actions = this.createActions();

        for (int index = 0; index < actions.length; index++) {
            panel.add(new JButton(actions[index]));
        }

        return panel;
    }

    protected JComponent createCenterPanel() {
        return this.diffPanelHelper.getDiffPanel().getComponent();
    }

    protected Action[] createActions() {
        Action closeAction =
            new AbstractAction("Close") {
                public void actionPerformed(ActionEvent actionevent) {
                    DiffViewerFrame.this.setVisible(false);
                    DiffViewerFrame.this.dispose();
                }
            };

        Action overwriteAction =
            new AbstractAction("Overwrite") {
                public void actionPerformed(ActionEvent actionevent) {
                    StringWriter writer = new StringWriter();
                    writer.write(content);

                    try {
                        ApplicationManager.getApplication().runWriteAction(new FileCreator(GenUtil.getOutputFile(ctx,
                                    ctx.getOutputFileName()), writer, ctx));
                        DiffViewerFrame.this.setVisible(false);
                        DiffViewerFrame.this.dispose();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

        return new Action[] { overwriteAction, closeAction };
    }

    private String buildTitle() {
        return new StringBuffer("Diff: ").append(this.diffPanelHelper.getTitle()).toString();
    }
}
