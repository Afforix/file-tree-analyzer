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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            FileInfo root = DiskExplorer.getFileTree("./testDir");

            Document result = FileInfoConverter.fileInfoToDom(root);

            //DateFormat for time attributes tests
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            //test root element
            Element resultRoot = result.getDocumentElement();

            assertEquals("directory", resultRoot.getTagName()); //root is directory
            assertTrue(resultRoot.hasAttribute("path")); //root has attribute path
            assertEquals(3, resultRoot.getChildNodes().getLength());
            assertEquals("3", resultRoot.getAttribute("numberOfFiles"));
            assertEquals("2", resultRoot.getAttribute("numberOfDirectories"));
            assertEquals(root.getName(), resultRoot.getAttribute("name"));

            assertEquals(dateFormat.format(root.getCreationTime()), resultRoot.getAttribute("creationTime"));

            //test first directory
            Element firstDirResult = (Element) resultRoot.getFirstChild();
            FileInfo firstDir = root.getChildren().get(0);

            assertEquals("directory", firstDirResult.getTagName());
            assertFalse(firstDirResult.hasAttribute("path")); //only root has attribute path
            assertEquals(2, firstDirResult.getChildNodes().getLength());
            assertEquals("2", firstDirResult.getAttribute("numberOfFiles"));
            assertEquals("0", firstDirResult.getAttribute("numberOfDirectories"));
            assertEquals(firstDir.getName(), firstDirResult.getAttribute("name"));
            assertFalse(firstDirResult.hasAttribute("size")); //only file have attribute size

            assertEquals(dateFormat.format(firstDir.getLastModifiedTime()), firstDirResult.getAttribute("lastModifiedTime"));

            //test file2.txt          
            Element file2Result = (Element) firstDirResult.getLastChild();
            FileInfo file2 = firstDir.getChildren().get(1);

            assertEquals("file", file2Result.getTagName());
            assertFalse(file2Result.hasAttribute("path")); //only root has attribute path
            assertFalse(file2Result.hasAttribute("numberOfFiles")); //only directories have attribute numberOfFiles
            assertFalse(file2Result.hasAttribute("numberOfDirectories")); //only directories have attribute numberOfDirectories
            assertEquals(0, file2Result.getChildNodes().getLength());
            assertEquals(file2.getName(), file2Result.getAttribute("name"));
            assertTrue(file2Result.hasAttribute("size"));

            assertEquals(dateFormat.format(file2.getLastAccessTime()), file2Result.getAttribute("lastAccessTime"));

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
