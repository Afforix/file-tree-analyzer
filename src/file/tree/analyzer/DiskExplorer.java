package file.tree.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for obtaining tree of directories, specified by the
 * given path.
 * 
 * @author afforix
 */
public class DiskExplorer {
    
    /** Class can't be instantiated */
    private DiskExplorer() {}

    /**
     * Method for obtaining directory tree.
     * 
     * @throws IOException if I/O error occurs
     * @param path root of the directory tree to analyze
     * @return root of the analyzed directory tree
     */
    public static FileInfo getFileTree(String path) throws IOException {
        FileVisitorImpl fileVisitor = new FileVisitorImpl();
        Path startPath = Paths.get(path);
        Files.walkFileTree(startPath, fileVisitor);
        return fileVisitor.getRoot();
    }
}
