package file.tree.analyzer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class provides methods used by {@code Files.walkFileTree()} and 
 * provides result of the disk analysis throughout method {@code getRoot()}.
 * This is just a utility class, most of the methods are implementation of 
 * the FileVisitor interface and should not be used on its own. Please use 
 * class which is designed to use this class correctly, for example 
 * DiskExplorer class.
 * 
 * @author afforix
 */
public class FileVisitorImpl implements FileVisitor<Path> {
    
    /** On top of the stack is currently processed directory */
    Deque<FileInfo> directoryStack = new ArrayDeque<>();
    /** Root of the directory tree to return */
    FileInfo root;
    
    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) throws IOException {
        FileInfo directory = new FileInfo(dirPath, attrs);
        if (directoryStack.isEmpty()) {
            //first directory to visit is root
            root = directory;            
        } else {
            //directoryStack.peek().addChild(directory);
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
        FileInfo failed = new FileInfo(file, exc);
        
        if (root == null) root = failed;
        else directoryStack.peek().addChild(failed);
        
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        directoryStack.peek().sortChildren();
        FileInfo directory = directoryStack.pop();
        
        if (!directoryStack.isEmpty()) {
            directoryStack.peek().addChild(directory);
        } else {
            //now we are back in root
            directory.sortChildren();
        }
        
        return FileVisitResult.CONTINUE;
    }
    
    /**
     * 
     * @return root of analyzed directory, null if no analysis has been done yet
     */
    public FileInfo getRoot() {
        return root;
    }
}
