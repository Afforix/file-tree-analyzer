/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Afforix
 */
public class FileInfoTest {
    
    public FileInfoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

     @Test
     public void testDirectory() {
         FileInfo dir = new FileInfo();
         dir.setDirectory(true);
         
         FileInfo file1 = new FileInfo();
         FileInfo file2 = new FileInfo();
         FileInfo dir2 = new FileInfo();
         dir2.setDirectory(true);
         
         dir2.addChildAndCount(file1);
         dir2.addChildAndCount(file2);
         
         dir.addChildAndCount(dir2);
         
         assertEquals(1, dir.getNumberOfDirectories());
         assertEquals(2, dir.getNumberOfFiles());
         assertEquals(0, dir2.getNumberOfDirectories());
         assertEquals(2, dir2.getNumberOfFiles());
         
         FileInfo file3 = new FileInfo();
         FileInfo dir3 = new FileInfo();
         dir3.setDirectory(true);
         dir2.addChild(file3);
         dir2.addChild(dir3);
         
         assertEquals(2, dir2.getNumberOfFiles());
         assertEquals(0, dir2.getNumberOfDirectories());
         
         assertEquals(4, dir2.getChildren().size());
     }
}
