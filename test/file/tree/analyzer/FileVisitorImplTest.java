/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Afforix
 */
public class FileVisitorImplTest {

    public FileVisitorImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestFileTree.createFileTree();
    }

    @AfterClass
    public static void tearDownClass() {
        TestFileTree.deleteFileTree();
    }
    
    /**
     * No file structure is stored in the visitor before being used.
     */
    @Test
    public void testNullRoot() {
        assertNull(new FileVisitorImpl().getRoot());
    }

    /**
     * Tests recursion realized by FileVisitorImpl when analyzing file tree.
     * However tested methods are not usually called directly by the programmer, 
     * they are designed to work with method Files.walkFileTree().
     * 
     * @throws IOException in case of error when working with files
     */
    @Test
    public void testRecursion() throws IOException {
        //fake atrributes for calling visitor's methods, usually created by 
        //system/java
        BasicFileAttributes attributes = new BasicFileAttributes() {

            @Override
            public FileTime lastModifiedTime() {
                return FileTime.fromMillis(2);
            }

            @Override
            public FileTime lastAccessTime() {
                return FileTime.fromMillis(1);
            }

            @Override
            public FileTime creationTime() {
                return FileTime.fromMillis(0);
            }

            @Override
            public boolean isRegularFile() {
                throw new UnsupportedOperationException("Not supported, not needed.");
            }

            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public boolean isOther() {
                throw new UnsupportedOperationException("Not supported, not needed.");
            }

            @Override
            public long size() {
                return 0;
            }

            @Override
            public Object fileKey() {
                throw new UnsupportedOperationException("Not supported, not needed.");
            }
        };
        
        FileVisitorImpl visitor = new FileVisitorImpl();
        visitor.preVisitDirectory(new File("testDir").toPath(), attributes);
        visitor.visitFileFailed(new File("testDir/file1.txt").toPath(), null);
        // | in this case attributes are not quite correct for regular file, but 
        // V they are not used all and the result will be consistent
        visitor.visitFile(new File("file2.txt").toPath(), attributes);
        
        visitor.preVisitDirectory(new File("testDir/dir1").toPath(), attributes);
        visitor.postVisitDirectory(new File("testDir/dir1").toPath(), null);
        
        visitor.postVisitDirectory(new File("testDir").toPath(), null);
        
        FileInfo root = visitor.getRoot();
        assertEquals("testDir", root.getName());
        assertTrue(root.isDirectory());
        assertFalse(root.getChildren().isEmpty());
        assertEquals(2, root.getNumberOfFiles());
        assertEquals(1, root.getNumberOfDirectories());
        
        FileInfo dir = root.getChildren().get(0);
        assertEquals("dir1", dir.getName());
        assertTrue(dir.isDirectory());
        assertTrue(dir.isAccessible());
        assertNotNull(dir.getPath());
        assertTrue(dir.getChildren().isEmpty());
        assertEquals(0, dir.getNumberOfDirectories());
        assertEquals(0, dir.getNumberOfFiles());
        
        FileInfo file = root.getChildren().get(1);
        assertEquals("file1.txt", file.getName());
        assertFalse(file.isAccessible());
        assertFalse(file.isDirectory());
        assertNotNull(file.getPath());
        assertNull(file.getCreationTime());
        assertNull(file.getLastAccessTime());
        assertNull(file.getLastModifiedTime());
        
        FileInfo file2 = root.getChildren().get(2);
        assertEquals("file2.txt", file2.getName());
        assertTrue(file2.isAccessible());
        assertFalse(file2.isDirectory());
        assertNotNull(file2.getPath());
        assertEquals(new Date(0), file2.getCreationTime());
        assertEquals(new Date(1), file2.getLastAccessTime());
        assertEquals(new Date(2), file2.getLastModifiedTime());
    }
}
