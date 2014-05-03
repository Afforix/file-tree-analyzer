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
    
    private DiskExplorer() {}

    /**
     * 
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
