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
import org.w3c.dom.Node;

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

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            childrenToDom(doc, doc, root);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private static void childrenToDom(Document doc, Node parent, FileInfo fileInfo) {
 
        Element currentElement = null;

        if (fileInfo.isDirectory()) {
            currentElement = doc.createElement("directory");
            currentElement.setAttribute("name", fileInfo.getName());
            currentElement.setAttribute("numberOfFiles", Integer.toString(fileInfo.getNumberOfFiles()));
            currentElement.setAttribute("numberOfDirectories", Integer.toString(fileInfo.getNumberOfDirectories()));
            //TODO size of folders?
            parent.appendChild(currentElement);
            List<FileInfo> children = new ArrayList<>(fileInfo.getChildren());
            for (FileInfo child : children) {
                childrenToDom(doc, currentElement, child);
            }
        } else {
            currentElement = doc.createElement("file");
            currentElement.setTextContent(fileInfo.getName());
            if (fileInfo.getSize() != null) {
                currentElement.setAttribute("size", fileInfo.getSize().toString());
            }
            parent.appendChild(currentElement);
        }

        if (fileInfo.isSymbolicLink()) {
            currentElement.setAttribute("symbolicLink", "true");
        } else {
            currentElement.setAttribute("symbolicLink", "false");
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        currentElement.setAttribute("creationTime", dateFormat.format(fileInfo.getCreationTime()));
        currentElement.setAttribute("lastAccessTime", dateFormat.format(fileInfo.getLastAccessTime()));
        currentElement.setAttribute("lastModifiedTime", dateFormat.format(fileInfo.getLastModifiedTime()));
        
        if(doc.getDocumentElement().equals(currentElement)) {
            currentElement.setAttribute("path", fileInfo.getPath());
        }
    }

    /**
     * Takes object representing XML DOM and converts it to FileInfo.
     *
     * @param doc XML Dom
     * @return FileInfo
     */
    public static FileInfo domToFileInfo(Document doc) {
        Element rootElement = doc.getDocumentElement();
        FileInfo root = childrenToFileInfo(rootElement);

        return root;
    }

    private static FileInfo childrenToFileInfo(Element parent) {
        //TODO path
        
        String name = "";
        boolean isDirectory = false;
        List<FileInfo> children = null;
        Long size = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;

        if (parent.getTagName().equals("directory")) {
            name = parent.getAttribute("name");
            isDirectory = true;
            numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
            numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));

            children = new ArrayList<>();
            for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                Element child = (Element) parent.getChildNodes().item(i);
                children.add(childrenToFileInfo(child));
            }
        } else {
            name = parent.getTextContent();
            size = Long.parseLong(parent.getAttribute("size"));
        }

        boolean isSymbolicLink = false;
        if (parent.getAttribute("isSymbolicLink").equals("true")) {
            isSymbolicLink = true;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
        try {
            creationTime = dateFormat.parse(parent.getAttribute("creationTime"));
            lastAccessTime = dateFormat.parse(parent.getAttribute("lastAccessTime"));
            lastModifiedTime = dateFormat.parse(parent.getAttribute("lastModifiedTime"));
        } catch (ParseException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileInfo root = new FileInfo(name, isDirectory, isSymbolicLink, size, creationTime,
                lastAccessTime, lastModifiedTime, children, numberOfFiles, numberOfDirectories);

        return root;
    }

}
