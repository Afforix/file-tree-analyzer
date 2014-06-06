/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

    /**
     * Test of fileInfoToDom method, of class FileInfoConverter.
     */
    @Test
    public void testFileInfoToDom() {
        System.out.println("fileInfoToDom test");
        FileInfo root = createTestFileInfo();
        
        Document expResult = createTestDocument();
        
        Document result = FileInfoConverter.fileInfoToDom(root);
        
        assertEquals(expResult.getDocumentElement().getNodeName(), result.getDocumentElement().getNodeName());
        assertEquals(expResult.getDocumentElement().getAttribute("name"), result.getDocumentElement().getAttribute("name"));
        assertEquals(expResult.getDocumentElement().getAttribute("symbolicLink"), result.getDocumentElement().getAttribute("symbolicLink"));
        assertEquals(expResult.getDocumentElement().getAttribute("accessible"), result.getDocumentElement().getAttribute("accessible"));
        assertEquals(expResult.getDocumentElement().getAttribute("path"), result.getDocumentElement().getAttribute("path"));
        assertEquals(expResult.getDocumentElement().getAttribute("numberOfFiles"), result.getDocumentElement().getAttribute("numberOfFiles"));
        assertEquals(expResult.getDocumentElement().getAttribute("numberOfDirectories"), result.getDocumentElement().getAttribute("numberOfDirectories"));
        assertEquals(expResult.getDocumentElement().getAttribute("creationTime"), result.getDocumentElement().getAttribute("creationTime"));
        assertEquals(expResult.getDocumentElement().getAttribute("lastAccesTime"), result.getDocumentElement().getAttribute("lasAccessTime"));
        assertEquals(expResult.getDocumentElement().getAttribute("lastModifiedTime"), result.getDocumentElement().getAttribute("lastModifiedTime"));
        
    }
    
    private FileInfo createTestFileInfo() {
        FileInfo root = new FileInfo();
        FileInfo child = new FileInfo();
        try {
            root.setPath("/home/testFolder");
            root.setAccessibility(true);
            root.setDirectory(true);
            root.setSymbolicLink(false);
            root.setName("testFolder");
            root.setNumberOfFiles(1);
            root.setNumberofDirectories(0);
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            root.setCreationTime(dateFormat.parse("2013-04-05T10:55:46"));
            root.setLastAccessTime(dateFormat.parse("2013-04-05T10:55:51"));
            root.setLastModifiedTime(dateFormat.parse("2013-04-05T10:55:46"));
            
            child.setPath("/home/testFolder/testFile.txt");
            child.setAccessibility(true);
            child.setName("testFile.txt");
            child.setSize(919l);
            child.setSymbolicLink(false);
            child.setDirectory(false);
            child.setCreationTime(dateFormat.parse("2014-06-05T10:55:46"));
            child.setLastAccessTime(dateFormat.parse("2014-06-05T10:55:51"));
            child.setLastModifiedTime(dateFormat.parse("2014-06-05T10:55:46"));
                        
            return root;
        } catch (ParseException ex) {
            //TODO
        }
        
        return root;
    }
    
    private Document createTestDocument() {
        Document doc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // create Document
            doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            //root directory
            Element rootElement = doc.createElement("directory");
            rootElement.setAttribute("name", "testFolder");
            rootElement.setAttribute("accessible", "true");
            rootElement.setAttribute("path", "/home/testFolder");
            rootElement.setAttribute("symbolicLink", "false");
            rootElement.setAttribute("numberOfFiles", "1");
            rootElement.setAttribute("numberOfDirectories", "0");
            rootElement.setAttribute("creationTime", "2013-04-05T10:55:46");
            rootElement.setAttribute("lastAccessTime", "2013-04-05T10:55:51");
            rootElement.setAttribute("lastModifiedTime", "2013-04-05T10:55:46");
            doc.appendChild(rootElement);

            //file element
            Element fileElement = doc.createElement("file");
            fileElement.setAttribute("name", "testFile.txt");
            fileElement.setAttribute("accessible", "true");
            fileElement.setAttribute("size", "919");
            fileElement.setAttribute("symbolicLink", "false");
            fileElement.setAttribute("creationTime", "2014-06-05T10:55:46");
            fileElement.setAttribute("lastAccessTime", "2014-06-05T10:55:51");
            fileElement.setAttribute("lastModifiedTime", "2014-06-05T10:55:46");
            rootElement.appendChild(fileElement);     
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileInfoConverterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
           
        return doc;
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
     * Test of domToDiffInfo method, of class FileInfoConverter.
     */
    @Test
    public void testDomToDiffInfo() {
        System.out.println("domToDiffInfo");
        Document doc = null;
        DiffInfo expResult = null;
        DiffInfo result = FileInfoConverter.domToDiffInfo(doc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of domToFileInfo2 method, of class FileInfoConverter.
     */
    @Test
    public void testDomToFileInfo2() {
        System.out.println("domToFileInfo2");
        Document doc = null;
        FileInfo expResult = null;
        FileInfo result = FileInfoConverter.domToFileInfo2(doc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of domToDiffInfo2 method, of class FileInfoConverter.
     */
    @Test
    public void testDomToDiffInfo2() {
        System.out.println("domToDiffInfo2");
        Document doc = null;
        DiffInfo expResult = null;
        DiffInfo result = FileInfoConverter.domToDiffInfo2(doc);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
