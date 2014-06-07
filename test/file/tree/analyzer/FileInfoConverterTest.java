/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author martina
 */
public class FileInfoConverterTest {

    @Before
    public void setUp() throws Exception {
        File root = new File(".", "testDir");
        if (root.mkdir()) {
            File f1 = new File(root, "file1.txt");
            f1.createNewFile();

            File d = new File(root, "dir1");
            if (d.mkdir()) {
                File f2 = new File(d, "file2.txt");
                f2.createNewFile();
                File f3 = new File(d, "file3.txt");
                f3.createNewFile();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        File root = new File(".", "testDir");

        File f = new File(root, "file1.txt");
        f.delete();

        File d = new File(root, "dir1");

        File f2 = new File(d, "file2.txt");
        f2.delete();

        File f3 = new File(d, "file3.txt");
        f3.delete();

        d.delete();

        root.delete();
    }

    /**
     * Test of fileInfoToDom method, of class FileInfoConverter.
     */
    @Test
    public void testFileInfoToDom() {
        System.out.println("fileInfoToDom test");
        try {
            FileInfo root = DiskExplorer.getFileTree("./testDir");
            
            Document result = FileInfoConverter.fileInfoToDom(root);
            
            //DateFormat for time attributes tests
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            
            //test root element
            Element resultRoot = result.getDocumentElement();
            
            assertEquals("directory", resultRoot.getTagName()); //root is directory
            assertTrue(resultRoot.hasAttribute("path")); //root has attribute path
            assertEquals(2, resultRoot.getChildNodes().getLength()); 
            assertEquals("3", resultRoot.getAttribute("numberOfFiles"));
            assertEquals("1", resultRoot.getAttribute("numberOfDirectories"));
            assertEquals(root.getName(), resultRoot.getAttribute("name"));
                       
            assertEquals(root.getCreationTime(), dateFormat.parse(resultRoot.getAttribute("creationTime")));
            
            //test first directory
            Element firstDirResult = (Element)resultRoot.getFirstChild();
            FileInfo firstDir = root.getChildren().get(0);
            
            assertEquals("directory", firstDirResult.getTagName()); 
            assertFalse(firstDirResult.hasAttribute("path")); //only root has attribute path
            assertEquals(2, firstDirResult.getChildNodes().getLength()); 
            assertEquals("2", firstDirResult.getAttribute("numberOfFiles")); 
            assertEquals("0", firstDirResult.getAttribute("numberOfDirectories"));
            assertEquals(firstDir.getName(), firstDirResult.getAttribute("name"));
            assertFalse(firstDirResult.hasAttribute("size")); //only file have attribute size
                       
            assertEquals(firstDir.getLastModifiedTime(), dateFormat.parse(firstDirResult.getAttribute("lastModifiedTime")));
            
            //test file2.txt          
            Element file2Result = (Element)firstDirResult.getLastChild();
            FileInfo file2 = firstDir.getChildren().get(1);
            
            assertEquals("file", file2Result.getTagName()); 
            assertFalse(file2Result.hasAttribute("path")); //only root has attribute path
            assertFalse(file2Result.hasAttribute("numberOfFiles")); //only directorie have attribute numberOfFiles
            assertFalse(file2Result.hasAttribute("numberOfDirectories")); //only directories have attribute numberOfDirectories
            assertEquals(0, file2Result.getChildNodes().getLength()); 
            assertEquals(file2.getName(), file2Result.getAttribute("name"));
            assertTrue(file2Result.hasAttribute("size"));
                       
            assertEquals(file2.getLastAccessTime(), dateFormat.parse(file2Result.getAttribute("lastAccessTime")));
            
        } catch (IOException ex) {
            fail("couldn't create FileInfo");
        } catch (ParseException ex) {
            fail("couldn't parse date");
        }
    }

    /**
     * Test of fileInfoToDom method with illegal argument.
     */
    @Test
    public void testNullFileInfoToDom() {
        System.out.println("fileInfoToDom with illegal argument test");

        try {
            Document result = FileInfoConverter.fileInfoToDom(null);
            fail("didn't throw IllegalArgumentException for invalid input");
        } catch (IllegalArgumentException ex) {

        }
    }

    /**
     * Test of domToFileInfo method, of class FileInfoConverter.
     */
    @Test
    public void testDomToFileInfo() {
        System.out.println("domToFileInfo");
        Document doc = null;
        FileInfo expResult = null;
        FileInfo result = FileInfoConverter.domToFileInfo(doc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of domToFileInfo method with illegal argument.
     */
    @Test
    public void testNullDomToFileInfo() {
        System.out.println("domToFileInfo with illegal argument test");

        try {
            FileInfo result = FileInfoConverter.domToFileInfo(null);
            fail("didn't throw IllegalArgumentException for invalid input");
        } catch (IllegalArgumentException ex) {

        }
    }

}
