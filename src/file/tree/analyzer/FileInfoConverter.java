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

            doc.getDocumentElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:noNamespaceSchemaLocation", "../analysesXmlSchema.xsd");

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private static void childrenToDom(Document doc, Node parent, FileInfo fileInfo) {

        Element currentElement = null;

        if (fileInfo.isDirectory()) {
            currentElement = doc.createElement("directory");
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
            if (fileInfo.getSize() != null) {
                currentElement.setAttribute("size", fileInfo.getSize().toString());
            }
            parent.appendChild(currentElement);
        }

        currentElement.setAttribute("name", fileInfo.getName());

        if (fileInfo.isSymbolicLink()) {
            currentElement.setAttribute("symbolicLink", "true");
        } else {
            currentElement.setAttribute("symbolicLink", "false");
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        currentElement.setAttribute("creationTime", dateFormat.format(fileInfo.getCreationTime()));
        currentElement.setAttribute("lastAccessTime", dateFormat.format(fileInfo.getLastAccessTime()));
        currentElement.setAttribute("lastModifiedTime", dateFormat.format(fileInfo.getLastModifiedTime()));

        if (doc.getDocumentElement().equals(currentElement)) {
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
        FileInfo root = childrenToFileInfo(rootElement, "");

        return root;
    }

    private static FileInfo childrenToFileInfo(Element parent, String path) {
        String name = "";
        boolean isDirectory = false;
        List<FileInfo> children = null;
        Long size = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;

        name = parent.getAttribute("name");

        if (parent.getTagName().equals("directory")) {
            isDirectory = true;
            numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
            numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));

            if (parent.hasAttribute("path")) {
                path = parent.getAttribute("path");
            } else {
                path += "/" + name;
            }

            children = new ArrayList<>();
            for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                Element child = (Element) parent.getChildNodes().item(i);
                children.add(childrenToFileInfo(child, path));
            }
        } else {
            size = Long.parseLong(parent.getAttribute("size"));
            path += "/" + name;
        }

        boolean isSymbolicLink = false;
        if (parent.getAttribute("isSymbolicLink").equals("true")) {
            isSymbolicLink = true;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

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

        FileInfo root = new FileInfo(name, path, isDirectory, isSymbolicLink, size, creationTime,
                lastAccessTime, lastModifiedTime, children, numberOfFiles, numberOfDirectories);

        return root;
    }

    /**
     * Takes object representing XML DOM and converts it to DiffInfo.
     *
     * @param doc XML Dom
     * @return FileInfo with DiffInfo
     */
    public static DiffInfo domToDiffInfo(Document doc) {
        Element rootElement = doc.getDocumentElement();
        DiffInfo root = childrenToDiffInfo(rootElement, "");

        return root;
    }
private static DiffInfo childrenToDiffInfo(Element parent, String path) {
        String name = "";
        boolean isDirectory = false;
        List<FileInfo> children = null;
        Long size = null;
        Long newSize = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        
        //DiffInfo lines - this is different from childrenToFileInfo
        String newString = ""; //temporary variable, not part of DiffInfo
        ItemState state = null;
        //end
        
        name = parent.getAttribute("name");
        System.out.println("NAME:" + name);
        
        if (parent.getTagName().equals("directory")) {
            isDirectory = true;
            numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
            numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));

            if (parent.hasAttribute("path")) {
                path = parent.getAttribute("path");
            } else {
                path += "/" + name;
            }
        
            children = new ArrayList<>();
            for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                //DiffInfo modification - added if()
                if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) parent.getChildNodes().item(i);
                    children.add(childrenToDiffInfo(child, path));
                }
            }
        } else {
            size = Long.parseLong(parent.getAttribute("size"));
            path += "/" + name;
            
            //DiffInfo lines
            newString = parent.getAttribute("newSize");
            if (!newString.isEmpty()) {
                newSize = Long.parseLong(newString);
            }
            //end
        }
        System.out.println("PATH:" + path);
                
        boolean isSymbolicLink = false;
        if (parent.getAttribute("isSymbolicLink").equals("true")) {
            isSymbolicLink = true;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
        System.out.println("*Time:");
        try {
            creationTime = dateFormat.parse(parent.getAttribute("creationTime"));
            System.out.println("CREATIONTIME:" + creationTime);
            lastAccessTime = dateFormat.parse(parent.getAttribute("lastAccessTime"));
            lastModifiedTime = dateFormat.parse(parent.getAttribute("lastModifiedTime"));
        } catch (ParseException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //DiffInfo lines
        Date newCreationTime = setDate(parent, creationTime, "newCreationTime", dateFormat);
        Date newLastAccessTime = setDate(parent, lastAccessTime, "newLastAccessTime", dateFormat);
        Date newLastModifiedTime = setDate(parent, lastModifiedTime, "newLastModifiedTime", dateFormat);
        
        newString = parent.getAttribute("state");
        if (!newString.isEmpty()) {
            state = ItemState.valueOf(parent.getAttribute("state"));
        } else {
            state = ItemState.created; //maybe choose other option
        }
        //end

        //DiffInfo lines, create new DiffInfo instead of new FileInfo
        DiffInfo root = new DiffInfo(name, path, isDirectory, isSymbolicLink, size, creationTime,
                lastAccessTime, lastModifiedTime, children, numberOfFiles, numberOfDirectories,
                state, newSize, newCreationTime, newLastAccessTime, newLastModifiedTime);
        //end
        
        return root; //DiffInfo type
    }

    /**
     * Gets date from desired attribute or uses "older" attribute.
     * @param parent element whose attribute should be tested
     * @param oldAttribute already existing Date (for example creationTime)
     * @param newAttributeName name of Date to set (for example newCreationTime)
     * @param dateFormat how should output look like
     * @return date
     */
    public static Date setDate(Element parent, Date oldAttribute, String newAttributeName, DateFormat dateFormat) {
        Date date = null;
        String newAttribute = parent.getAttribute(newAttributeName);
        System.out.println("DATUM:" + oldAttribute);
        
        if (!(newAttribute.isEmpty())) {
            try {

                date = dateFormat.parse(newAttribute);
            } catch (ParseException ex) {
                Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else { //use value from already existing attribute
            //TODO: try if new*Time can be null && not throw exception
            date = oldAttribute;
        }

        return date;
    }
}
