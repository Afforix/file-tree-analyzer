package file.tree.analyzer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class provides methods used by Files.walkFileTree().
 * Structure of the traversed directory tree is stored 
 * and can be obtained by method getRoot().
 * 
 * @author afforix
 */
public class FileVisitorImpl implements FileVisitor<Path> {
    
    //on top of the stack is currently processed directory
    Deque<FileInfo> directoryStack = new ArrayDeque<>();
    FileInfo root;
    
    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) throws IOException {
        FileInfo directory = new FileInfo(dirPath, attrs);
        if (directoryStack.isEmpty()) {
            root = directory;            
        } else {
            directoryStack.peek().addChild(directory);
        }
        directoryStack.push(directory);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
        FileInfo file = new FileInfo(filePath, attrs);
        if (directoryStack.isEmpty()) {
            root = file;
        } else {
            directoryStack.peek().addChild(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("File failed: " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        directoryStack.peek().sortChildren();
        directoryStack.pop();
        return FileVisitResult.CONTINUE;
    }
    
    /**
     * 
     * @return root of analyzed directory, null if no
     *          analysis has been done yet
     */
    public FileInfo getRoot() {
        return root;
    }
}
