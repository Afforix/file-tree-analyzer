package file.tree.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author afforix
 */
public class DiskExplorer {

    /**
     * 
     * @param path root of the directory tree to analyze
     * @return root of the analyzed directory tree
     */
    public FileInfo getFileTree(String path) {
        FileVisitorImpl fileVisitor = new FileVisitorImpl();
        Path startPath = Paths.get(path);
        try {
            Files.walkFileTree(startPath, fileVisitor);
        } catch (IOException ex) {
            System.err.println("walkFileTree failed " + ex);
            return null;
        }
        return fileVisitor.getRoot();
    }
}
