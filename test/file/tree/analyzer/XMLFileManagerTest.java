/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 *
 * @author martina
 */
public class XMLFileManagerTest {

    private XMLFileManager manager;

    public XMLFileManagerTest() {
        manager = new XMLFileManager("./testDir");
    }

    @Before
    public void setUp() throws Exception {
        File testDir = new File(".", "testDir");
        if (testDir.mkdir()) {
            File f1 = new File(testDir, "file1.xml");
            File f2 = new File(testDir, "file2.xml");
            File f3 = new File(testDir, "file3.xml");

            f1.createNewFile();
            f2.createNewFile();
            f3.createNewFile();
        }
    }

    @After
    public void tearDown() throws Exception {
        File testDir = new File(".", "testDir");

        if (testDir.exists()) {
            File f1 = new File(testDir, "file1.xml");
            File f2 = new File(testDir, "file2.xml");
            File f3 = new File(testDir, "file3.xml");

            f1.delete();
            f2.delete();
            f3.delete();

            testDir.delete();
        }

    }

    /**
     * Test of createXMLFile method, of class XMLFileManager.
     */
    @Test
    public void testCreateXMLFile() {
        System.out.println("createXMLFile test");
        Document dom = null;
        
        try {
            dom = FileInfoConverter.fileInfoToDom(DiskExplorer.getFileTree("./testDir"));
        } catch (IOException ex) {
            fail("could not create FileInfo");
        }
        
        String fileName = manager.createXMLFile(dom);
        
        File file = new File("./testDir", fileName);
        assertTrue(file.exists());
        
        if(file.exists()) {
            file.delete();
        }
    }

    /**
     * Test of createXMLFile method with illegal attribute.
     */
    @Test
    public void testCreateNullXMLFile() {
        System.out.println("createXMLFile with illegal attribute test");

        try {
            manager.createXMLFile(null);
            fail("didn't throw IllegalArgumentException for invalid input");
        } catch (IllegalArgumentException ex) {

        }

    }

    /**
     * Test of createXMLFile method, of class XMLFileManager.
     */
    @Test
    public void testDeleteXMLFile() {
        System.out.println("deleteXMLFile");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteXMLFile method with illegal attribute.
     */
    @Test
    public void testDeleteNullXMLFile() {
        System.out.println("deleteXMLFile with illegal attribute");

        try {
            manager.deleteXMLFile(null);
            fail("didn't throw IllegalArgumentException for invalid input");
        } catch (IllegalArgumentException ex) {

        }

    }

    /**
     * Test of findAllXMLFiles method, of class XMLFileManager.
     */
    @Test
    public void testFindAllXMLFiles() {
        System.out.println("findAllXMLFiles");
        XMLFileManager instance = null;
        List<String> expResult = null;
        List<String> result = instance.findAllXMLFiles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findXMLFile method, of class XMLFileManager.
     */
    @Test
    public void testFindXMLFile_String() {
        System.out.println("findXMLFile");
        String fileName = "";
        XMLFileManager instance = null;
        Document expResult = null;
        Document result = instance.findXMLFile(fileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findXMLFile method, of class XMLFileManager.
     */
    @Test
    public void testFindXMLFile_String_Boolean() {
        System.out.println("findXMLFile");
        String fileName = "";
        Boolean turnOffDefferedDom = null;
        XMLFileManager instance = null;
        Document expResult = null;
        Document result = instance.findXMLFile(fileName, turnOffDefferedDom);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
