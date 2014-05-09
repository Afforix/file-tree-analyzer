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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author martina
 */
public class FileInfoConverter {

    /**
     * Takes object representing root of the directory tree and converts it to
     * XML DOM.
     *
     * @param root root of the directory tree
     * @return XML DOM
     */
    public static Document fileInfoToDom(FileInfo root) {
        Document doc = null;
        Element rootElement = null;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            // root element
            if (root.isDirectory()) {
                rootElement = doc.createElement("directory");
                rootElement.setAttribute("name", root.getName());
                rootElement.setAttribute("numberOfFiles", Integer.toString(root.getNumberOfFiles()));
                rootElement.setAttribute("numberOfDirectories", Integer.toString(root.getNumberOfDirectories()));
                doc.appendChild(rootElement);
                List<FileInfo> children = new ArrayList<>(root.getChildren());
                for (FileInfo child : children) {
                    childrenToDom(doc, rootElement, child);
                }
            } else {
                rootElement = doc.createElement("file");
                rootElement.setTextContent(root.getName());
                if (root.getSize() != null) {
                    rootElement.setAttribute("size", root.getSize().toString());
                }
                doc.appendChild(rootElement);
            }

            if (root.isSymbolicLink()) {
                rootElement.setAttribute("symbolicLink", "true");
            } else {
                rootElement.setAttribute("symbolicLink", "false");
            }

            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //TODO

            rootElement.setAttribute("creationTime", dateFormat.format(root.getCreationTime()));
            rootElement.setAttribute("lastAccessTime", dateFormat.format(root.getLastAccessTime()));
            rootElement.setAttribute("lastModifiedTime", dateFormat.format(root.getLastModifiedTime()));

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private static void childrenToDom(Document doc, Element parent, FileInfo root) {
        Element currentElement = null;

        if (root.isDirectory()) {
            currentElement = doc.createElement("directory");
            currentElement.setAttribute("name", root.getName());
            currentElement.setAttribute("numberOfFiles", Integer.toString(root.getNumberOfFiles()));
            currentElement.setAttribute("numberOfDirectories", Integer.toString(root.getNumberOfDirectories()));
            //TODO size of the directory?
            parent.appendChild(currentElement);
            List<FileInfo> children = new ArrayList<>(root.getChildren());
            for (FileInfo child : children) {
                childrenToDom(doc, currentElement, child);
            }
        } else {
            currentElement = doc.createElement("file");
            currentElement.setTextContent(root.getName());
            if (root.getSize() != null) {
                currentElement.setAttribute("size", root.getSize().toString());
            }
            parent.appendChild(currentElement);
        }

        if (root.isSymbolicLink()) {
            currentElement.setAttribute("symbolicLink", "true");
        } else {
            currentElement.setAttribute("symbolicLink", "false");
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //TODO

        currentElement.setAttribute("creationTime", dateFormat.format(root.getCreationTime()));
        currentElement.setAttribute("lastAccessTime", dateFormat.format(root.getLastAccessTime()));
        currentElement.setAttribute("lastModifiedTime", dateFormat.format(root.getLastModifiedTime()));
    }

    /**
     * Takes object representing XML DOM and converts it to FileInfo.
     *
     * @param doc XML Dom
     * @return FileInfo
     */
    public static FileInfo domToFileInfo(Document doc) {

        Element rootElement = doc.getDocumentElement();

        String name = "";
        boolean isDirectory = false;
        List<FileInfo> children = null;
        Long size = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        
        if (rootElement.getTagName().equals("directory")) {
            name = rootElement.getAttribute("name");
            isDirectory = true;
            numberOfFiles = Integer.parseInt(rootElement.getAttribute("numberOfFiles"));
            numberOfDirectories = Integer.parseInt(rootElement.getAttribute("numberOfDirectories"));
        } else {
            name = rootElement.getTextContent();
            size = Long.parseLong(rootElement.getAttribute("size"));
        }

        boolean isSymbolicLink = false;
        if (rootElement.getAttribute("isSymbolicLink").equals("true")) {
            isSymbolicLink = true;
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //TODO

        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
        try {
            creationTime = dateFormat.parse(rootElement.getAttribute("creationTime"));
            lastAccessTime = dateFormat.parse(rootElement.getAttribute("lastAccessTime"));
            lastModifiedTime = dateFormat.parse(rootElement.getAttribute("lastModifiedTime"));
        } catch (ParseException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
       

        FileInfo root = new FileInfo(name, isDirectory, isSymbolicLink, size, creationTime,
                lastAccessTime, lastModifiedTime, children, numberOfFiles, numberOfDirectories);

        return root;
    }

    //TODO delete, only for testing
    public static void main(String[] args) throws IOException {
        XMLFileManager xM = new XMLFileManager("/home/martina/Dokumenty/PB167");
        Document doc = xM.findXMLFile("test.xml");
        FileInfo root = domToFileInfo(doc);
    }

}
