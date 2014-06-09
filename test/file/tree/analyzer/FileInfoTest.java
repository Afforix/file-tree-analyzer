/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Afforix
 */
public class FileInfoTest {
    
    public FileInfoTest() {
    }
    
    @Test
    public void testFile() {
        FileInfo file = new FileInfo();
        file.setName("testFile");
        file.setPath("/testFile");
        file.setSize(Long.valueOf(42));
        file.setCreationTime(new Date(0));
        file.setLastAccessTime(new Date(1));
        file.setLastModifiedTime(new Date(2));
        file.setSymbolicLink(false);
        file.setAccessibility(true);
        
        assertEquals("testFile", file.getName());
        assertEquals("/testFile", file.getPath());
        assertEquals(Long.valueOf(42), file.getSize());
        assertEquals(new Date(0), file.getCreationTime());
        assertEquals(new Date(1), file.getLastAccessTime());
        assertEquals(new Date(2), file.getLastModifiedTime());
        assertFalse(file.isSymbolicLink());
        assertFalse(file.isDirectory());
        assertTrue(file.isAccessible());
    }

     @Test
     public void testDirectory() {
         FileInfo dir = new FileInfo();
         dir.setDirectory(true);
         dir.setAccessibility(true);
         
         assertTrue(dir.getChildren().isEmpty());
         
         FileInfo file1 = new FileInfo();
         file1.setName("file1");
         FileInfo file2 = new FileInfo();
         file2.setName("file2");
         FileInfo dir2 = new FileInfo();
         dir2.setDirectory(true);
         dir2.setAccessibility(true);
         
         dir2.addChildAndCount(file2);
         dir2.addChildAndCount(file1);
         
         dir.addChildAndCount(dir2);
         
         assertEquals(1, dir.getNumberOfDirectories());
         assertEquals(2, dir.getNumberOfFiles());
         assertEquals(0, dir2.getNumberOfDirectories());
         assertEquals(2, dir2.getNumberOfFiles());
         
         FileInfo file3 = new FileInfo();
         file3.setName("file3");
         FileInfo dir3 = new FileInfo();
         dir3.setDirectory(true);
         dir3.setName("dir3");
         dir2.addChild(file3);
         dir2.addChild(dir3);
         
         assertEquals(2, dir2.getNumberOfFiles());
         assertEquals(0, dir2.getNumberOfDirectories());
         
         assertEquals(4, dir2.getChildren().size());
         assertEquals("file2", dir2.getChildren().get(0).getName());
         assertEquals("file1", dir2.getChildren().get(1).getName());
         assertEquals("file3", dir2.getChildren().get(2).getName());
         assertEquals("dir3", dir2.getChildren().get(3).getName());
         dir2.sortChildren();
         assertEquals("dir3", dir2.getChildren().get(0).getName());
         assertEquals("file1", dir2.getChildren().get(1).getName());
         assertEquals("file2", dir2.getChildren().get(2).getName());
         assertEquals("file3", dir2.getChildren().get(3).getName());
     }
     
     @Test
     public void testRealDirectory() throws IOException {
         TestFileTree.createFileTree();
         FileInfo root = new FileInfo(new File("testDir").toPath());
         
         assertEquals("testDir", root.getName());
         assertNotNull(root.getPath());
         assertTrue(root.isDirectory());
         
         TestFileTree.deleteFileTree();
     }
}
