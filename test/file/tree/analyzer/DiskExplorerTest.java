/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author martina
 */
public class DiskExplorerTest {
    
    private boolean testSymLink;

    @Before
    public void setUp() throws Exception {
        testSymLink = TestFileTree.createFileTree();
    }

    @After
    public void tearDown() {
        TestFileTree.deleteFileTree();
    }

    /**
     * Test of getFileTree method, of class DiskExplorer.
     */
    @Test
    public void testGetFileTree() {
        System.out.println("getFileTree test");

        FileInfo result = null;
        try {
            result = DiskExplorer.getFileTree("./testDir");
        } catch (IOException ex) {
            fail("could not create FileInfo");
        }
        
        //test testDir
        File rootDir = new File(".", "testDir");

        assertEquals(rootDir.isDirectory(), result.isDirectory()); 
        assertEquals(2, result.getChildren().size());
        assertEquals(3, result.getNumberOfFiles());
        assertEquals(1, result.getNumberOfDirectories());
        assertEquals(rootDir.getName(), result.getName());

        assertEquals(rootDir.lastModified(), result.getLastModifiedTime().getTime());
        
        //test file1.txt
        File f1 = new File(rootDir.getPath(), "file1.txt");
        FileInfo file1Result = result.getChildren().get(1);

        assertEquals(f1.isDirectory(), file1Result.isDirectory());
        assertEquals(f1.getAbsolutePath(), file1Result.getPath());
        assertEquals(0, file1Result.getNumberOfFiles());
        assertEquals(0, file1Result.getNumberOfDirectories());
        assertEquals(f1.getName(), file1Result.getName());
        assertEquals((Long)f1.length(), (Long)file1Result.getSize()); 

        //test dir1
        File dir1 = new File(rootDir.getPath(), "dir1");
        FileInfo dir1Result = result.getChildren().get(0);

        assertEquals(dir1.isDirectory(), dir1Result.isDirectory());
        assertEquals(dir1.getAbsolutePath(), dir1Result.getPath());
        assertEquals(2, dir1Result.getChildren().size());
        assertEquals(2, dir1Result.getNumberOfFiles());
        assertEquals(0, dir1Result.getNumberOfDirectories());
        assertEquals(dir1.getName(), dir1Result.getName());

        //test file2.txt          
        File f2 = new File(dir1.getPath(), "file2.txt");
        FileInfo file2Result = dir1Result.getChildren().get(0);

        assertEquals(f2.isDirectory(), file2Result.isDirectory());
        assertEquals(f2.getAbsolutePath(), file2Result.getPath());
        assertEquals(0, file2Result.getNumberOfFiles());
        assertEquals(0, file2Result.getNumberOfDirectories());
        assertEquals(f2.getName(), file2Result.getName());
        
        if(testSymLink) {
            assertTrue(file2Result.isSymbolicLink());
        }
    }

}
