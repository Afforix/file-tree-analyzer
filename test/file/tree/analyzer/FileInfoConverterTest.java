/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.IOException;
import java.text.DateFormat;
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
//    private boolean testSymLink;
    
    @Before
    public void setUp() throws Exception {
        TestFileTree.createFileTree();
    }

    @After
    public void tearDown() throws Exception {
         TestFileTree.deleteFileTree();
    }

    /**
     * Test of fileInfoToDom method, of class FileInfoConverter.
     */
    @Test
    public void testFileInfoToDom() {
        System.out.println("fileInfoToDom test");
        try {
            FileInfo expRoot = DiskExplorer.getFileTree("./testDir");

            Document resultDoc = FileInfoConverter.fileInfoToDom(expRoot);

            //DateFormat for time attributes tests
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            //test root element
            Element resultRoot = resultDoc.getDocumentElement();

            assertEquals("directory", resultRoot.getTagName()); //root is directory
            assertTrue(resultRoot.hasAttribute("path")); //root has attribute path
            assertEquals(3, resultRoot.getChildNodes().getLength());
            assertEquals("4", resultRoot.getAttribute("numberOfFiles"));
            assertEquals("2", resultRoot.getAttribute("numberOfDirectories"));
            assertEquals(expRoot.getName(), resultRoot.getAttribute("name"));

            assertEquals(dateFormat.format(expRoot.getCreationTime()), resultRoot.getAttribute("creationTime"));

            //test first directory
            Element result = (Element) resultRoot.getFirstChild();
            FileInfo exp = expRoot.getChildren().get(0);

            assertEquals("directory", result.getTagName());
            assertFalse(result.hasAttribute("path")); //only root has attribute path
            assertEquals(2, result.getChildNodes().getLength());
            assertEquals("2", result.getAttribute("numberOfFiles"));
            assertEquals("0", result.getAttribute("numberOfDirectories"));
            assertEquals(exp.getName(), result.getAttribute("name"));
            assertFalse(result.hasAttribute("size")); //only files have attribute size

            assertEquals(dateFormat.format(exp.getLastModifiedTime()), result.getAttribute("lastModifiedTime"));

            //test file2.txt          
            result = (Element) result.getLastChild();
            exp = exp.getChildren().get(1);

            assertEquals("file", result.getTagName());
            assertFalse(result.hasAttribute("path")); //only root has attribute path
            assertFalse(result.hasAttribute("numberOfFiles")); //only directories have attribute numberOfFiles
            assertFalse(result.hasAttribute("numberOfDirectories")); //only directories have attribute numberOfDirectories
            assertEquals(0, result.getChildNodes().getLength());
            assertEquals(exp.getName(), result.getAttribute("name"));
            assertTrue(result.hasAttribute("size"));

            assertEquals(dateFormat.format(exp.getLastAccessTime()), result.getAttribute("lastAccessTime"));
            
            //test dir2
            result = (Element) resultRoot.getChildNodes().item(1);
            exp = expRoot.getChildren().get(1);
            
            assertEquals("directory", result.getTagName());
            assertFalse(result.hasAttribute("path")); 
            assertEquals(1, result.getChildNodes().getLength());
            assertEquals("1", result.getAttribute("numberOfFiles"));
            assertEquals("0", result.getAttribute("numberOfDirectories"));
            assertEquals(exp.getName(), result.getAttribute("name"));
            assertFalse(result.hasAttribute("size")); 

            assertEquals(dateFormat.format(exp.getLastAccessTime()), result.getAttribute("lastAccessTime"));

        } catch (IOException ex) {
            fail("couldn't create FileInfo");
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
    public void testDomToFileInfo() throws Exception{
        System.out.println("domToFileInfo test");
        XMLFileManager manager = new XMLFileManager("./test/file/tree/analyzer");
        Document doc = manager.findXMLFile("testXMLDocument.xml");
        
        //DateFormat for time attributes tests
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        //test root
        FileInfo result = FileInfoConverter.domToFileInfo(doc);
        Element exp = doc.getDocumentElement();
        
        assertTrue(result.isDirectory());
        assertFalse(result.isSymbolicLink());
        assertEquals(exp.getAttribute("name"), result.getName());
        assertEquals(exp.getAttribute("numberOfFiles"), "" + result.getNumberOfFiles());
        assertEquals(exp.getAttribute("numberOfDirectories"), "" + result.getNumberOfDirectories());
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime()));
        assertTrue(exp.hasAttribute("path"));
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime()));
        assertEquals(exp.getAttribute("accessible"), Boolean.toString(result.isAccessible()));
        assertEquals(exp.getAttribute("symbolicLink"), Boolean.toString(result.isSymbolicLink()));
        
        //test first directory
        result = result.getChildren().get(0);
        exp = (Element)exp.getChildNodes().item(1);
        
        assertTrue(result.isDirectory());
        assertFalse(result.isSymbolicLink());
        assertEquals(exp.getAttribute("name"), result.getName());
        assertEquals(exp.getAttribute("numberOfFiles"), "" + result.getNumberOfFiles());
        assertEquals(exp.getAttribute("numberOfDirectories"), "" + result.getNumberOfDirectories());
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime()));
        assertFalse(exp.hasAttribute("path"));
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime())); 
        assertEquals(exp.getAttribute("accessible"), Boolean.toString(result.isAccessible()));
        assertEquals(exp.getAttribute("symbolicLink"), Boolean.toString(result.isSymbolicLink()));
        
        //test first file
        result = result.getChildren().get(1);
        exp = (Element)exp.getChildNodes().item(3);
        
        assertFalse(result.isDirectory());
        assertFalse(result.isSymbolicLink());
        assertEquals(exp.getAttribute("name"), result.getName());
        assertFalse(exp.hasAttribute("numberOfFiles"));
        assertFalse(exp.hasAttribute("numberOfDirectories"));
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime()));
        assertFalse(exp.hasAttribute("path"));
        assertEquals(exp.getAttribute("creationTime"), dateFormat.format(result.getCreationTime())); 
        assertEquals(exp.getAttribute("accessible"), Boolean.toString(result.isAccessible()));
        assertEquals(exp.getAttribute("symbolicLink"), Boolean.toString(result.isSymbolicLink()));
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
