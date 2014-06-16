package file.tree.analyzer.gui;

import file.tree.analyzer.DiskExplorer;
import file.tree.analyzer.FileInfo;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author ansy
 */
public class DiskExplorerTask extends Task<FileInfo> {

    File directory;

    DiskExplorerTask(File directory) {
        this.directory = directory;
    }

    @Override
    protected FileInfo call() throws Exception {
        try {
            return DiskExplorer.getFileTree(directory.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
