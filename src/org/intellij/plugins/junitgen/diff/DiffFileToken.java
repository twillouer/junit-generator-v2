package org.intellij.plugins.junitgen.diff;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;


/**
 * A basic Java bean that holds information about a file that is relevant for a diff viewer.
 */
public final class DiffFileToken {
    private String name;
    private String title;
    private String contents;
    private FileType type;

    public DiffFileToken() {}

    public DiffFileToken(String name, String title, String fileContents, FileType fileType) {
        this.name = name;
        this.title = title;
        this.contents = fileContents;
        this.type = fileType;
    }

    /**
     * Creates a token based upon the specified virtual file. The file's path is used for the name,
     * the contents are extracted, and the file type is inferred from the file's extension.
     *
     * @param file  The file the token will be based upon.
     * @return  A new DiffFileToken.
     * @throws IOException  If the contents of the virtual file can not be read.
     */
    public static DiffFileToken createForVirtualFile(VirtualFile file)
        throws IOException {
        return new DiffFileToken(file.getName(), file.getPath(), String.valueOf(file.contentsToByteArray()),
            FileTypeManager.getInstance().getFileTypeByExtension(file.getExtension()));
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public FileType getType() {
        return this.type;
    }

    public void setType(FileType type) {
        this.type = type;
    }
}
