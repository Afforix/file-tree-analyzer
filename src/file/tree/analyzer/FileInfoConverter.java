

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author martina
 */
public class FileInfoConverter {

    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
    
    /**
     * Takes object representing root of the directory tree and converts it to
     * XML DOM.
     *
     * @param root root of the directory tree
     * @return XML DOM
     */
    public static Document fileInfoToDom(FileInfo root) {
        if(root == null) throw new IllegalArgumentException("root is null");
        
        Document doc = null;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            //create new document
            doc = docBuilder.newDocument();
            doc.setXmlVersion("1.0");

            childrenToDom(doc, doc, root);

            //add xml schema
            doc.getDocumentElement().setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:noNamespaceSchemaLocation", "../analysesXmlSchema.xsd");

        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private static void childrenToDom(Document doc, Node parent, FileInfo fileInfo) {
        //create new element that represents fileInfo and add all attributes
        
        Element currentElement;

        if (fileInfo.isDirectory()) {
            currentElement = doc.createElement("directory");
            if (fileInfo.isAccessible()) {
                currentElement.setAttribute("numberOfFiles", Integer.toString(fileInfo.getNumberOfFiles()));
                currentElement.setAttribute("numberOfDirectories", Integer.toString(fileInfo.getNumberOfDirectories()));
                for (FileInfo child : fileInfo.getChildren()) {
                    childrenToDom(doc, currentElement, child);
                }
            }
        } else {
            currentElement = doc.createElement("file");
            if (fileInfo.isAccessible()) {
                currentElement.setAttribute("size", fileInfo.getSize().toString());
            }
        }

        parent.appendChild(currentElement);
        currentElement.setAttribute("name", fileInfo.getName());

        if (fileInfo.isAccessible()) {
            currentElement.setAttribute("accessible", "true");
            //set date format for time attributes
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            currentElement.setAttribute("creationTime", dateFormat.format(fileInfo.getCreationTime()));
            currentElement.setAttribute("lastAccessTime", dateFormat.format(fileInfo.getLastAccessTime()));
            currentElement.setAttribute("lastModifiedTime", dateFormat.format(fileInfo.getLastModifiedTime()));

            currentElement.setAttribute("symbolicLink", Boolean.toString(fileInfo.isSymbolicLink()));
        } else {
            currentElement.setAttribute("accessible", "false");
        }

        //it works thanks to the == :-)
        if (doc.getDocumentElement() == currentElement) {
            currentElement.setAttribute("path", fileInfo.getPath());
        }
    }

    /**
     * Takes object representing XML DOM and converts it to FileInfo.
     *
     * @param doc XML DOM
     * @return FileInfo
     *  
     */
    /*
    public static FileInfo domToFileInfo(Document doc) {
        if(doc == null) throw new IllegalArgumentException("doc is null");
        
        Element rootElement = doc.getDocumentElement();
        FileInfo root = childrenToFileInfo(rootElement, "");

        return root;
    }
    */

    /**
     * @deprecated
     */
    /*
    private static FileInfo childrenToFileInfo(Element element, String path) {
        //get all file info from element
        
        FileInfo item = new FileInfo();

        item.setName(element.getAttribute("name"));
        item.setAccessibility(Boolean.parseBoolean(element.getAttribute("accessible")));
        item.setDirectory(element.getTagName().equals("directory"));
        item.setPath(element.hasAttribute("path") ? element.getAttribute("path") : path + "/" + item.getName());

        if (!item.isAccessible()) {
            return item;//we are done here
        }

        if (item.isDirectory()) {
            item.setNumberOfFiles(Integer.parseInt(element.getAttribute("numberOfFiles")));
            item.setNumberOfDirectories(Integer.parseInt(element.getAttribute("numberOfDirectories")));

            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                Node node = element.getChildNodes().item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                item.addChild(childrenToFileInfo((Element) node, item.getPath()));
            }
        } else {
            item.setSize(Long.parseLong(element.getAttribute("size")));
        }

        item.setSymbolicLink(Boolean.parseBoolean(element.getAttribute("symbolicLink")));
        //set date format for time attributes
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        try {
            item.setCreationTime(dateFormat.parse(element.getAttribute("creationTime")));
            item.setLastAccessTime(dateFormat.parse(element.getAttribute("lastAccessTime")));
            item.setLastModifiedTime(dateFormat.parse(element.getAttribute("lastModifiedTime")));
        } catch (ParseException ex) {
           logger.log(Level.SEVERE, null, ex);
        }

        return item;
    }
    */
    
    /**
     * Gets date from desired attribute or uses "older" attribute.
     *
     * @param parent element whose attribute should be tested
     * @param oldAttribute already existing Date (for example creationTime)
     * @param newAttributeName name of Date to set (for example newCreationTime)
     * @param dateFormat how should output look like
     * @return date
     */
    private static Date setDate(Element parent, Date oldAttribute, String newAttributeName, DateFormat dateFormat) {
        Date date = null;
        String newAttribute = parent.getAttribute(newAttributeName);

        if (!(newAttribute.isEmpty())) {
            try {

                date = dateFormat.parse(newAttribute);
            } catch (ParseException ex) {
               logger.log(Level.SEVERE, null, ex);
            }
        } else { //use value from already existing attribute
            date = oldAttribute;
        }

        return date;
    }
    
    /**
     * Set newNumberOfFiles or newNumberOfDirectories 
     * @param newAttributeValue string to parse value from
     * @param state 
     * @param oldAttributeValue
     * @return int
     */
    private static int setNewNumber(String newAttributeValue, ItemState state, int oldAttributeValue) {
        int number;
        if (!newAttributeValue.isEmpty()) {
            number = Integer.parseInt(newAttributeValue);
        /*} else if (state == ItemState.DELETED) {
            number = 0; */
        } else {
            number = oldAttributeValue;
        }
        return number;
    }
    
    private static boolean setNewBoolean(String newAttributevalue, boolean oldAttributeValue) {
        boolean bool;
        if (!newAttributevalue.isEmpty()) {
                bool = Boolean.getBoolean(newAttributevalue);
            } else {
                bool = oldAttributeValue;
            }
        return bool;
    }

    /**
     * Takes object representing XML DOM and converts it to FileInfo.
     *
     * @param doc XML Dom
     * @return FileInfo
     */
    //name changed from domToFileInfo2
    public static FileInfo domToFileInfo(Document doc) {
        if(doc == null) throw new IllegalArgumentException("doc is null");

        Element rootElement = doc.getDocumentElement();
        FileInfo root = childrenToInfo(rootElement, "", false);

        return root;
    }

    /**
     * Takes object representing XML DOM and converts it to DiffInfo.
     *
     * @param doc XML Dom
     * @return FileInfo with DiffInfo
     */
    public static DiffInfo domToDiffInfo(Document doc) {
        if(doc == null) throw new IllegalArgumentException("doc is null");

        Element rootElement = doc.getDocumentElement();
        DiffInfo root = (DiffInfo) childrenToInfo(rootElement, "", true);

        return root;
    }

    /**
     *
     * @param parent Element to be processed
     * @param path To parent element
     * @param isDiffInfo DiffInfo needs additional code
     * @return Object. This can be casted back to FileInfo or DiffInfo
     */
    private static FileInfo childrenToInfo(Element parent, String path, boolean isDiffInfo) {
        String name = "";
        FileInfo fileInfoRoot = null; 

        boolean isDirectory = false;
        boolean isAccessible = false;
        boolean isSymbolicLink = false;

        Long size = null;
        Long newSize = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
                
        //variables for DiffInfo
        ItemState state = null; 
        DiffInfo diffInfoRoot = null;
        int newNumberOfFiles = 0;
        int newNumberOfDirectories = 0;
        boolean isNewlyAccessible = false;
        boolean isNewlySymbolicLink = false;
        boolean isChangedFromAccessibleToUnaccessible = false; //if true, copy values from old attributes to new*
        
        Date newCreationTime = null;
        Date newLastAccessTime = null;
        Date newLastModifiedTime = null;

        String newString = "";  //temporary variable
        
        //always get: path, isAccessible, isDirectory, isSymbolicLink
        name = parent.getAttribute("name");
        if (isDirectory && parent.hasAttribute("path")) {
            path = parent.getAttribute("path");
        } else {
            path += "/" + name;
        }
        
        isAccessible = parent.getAttribute("accessible").equals("true");
        isDirectory = parent.getTagName().equals("directory");
        isSymbolicLink = parent.getAttribute("symbolicLink").equals("true");
        
        //if isAccessible: get size/(numberOfFiles, numberOfDirectories), creatT, lastAccT, lastModT, 
        if (isAccessible) {
            if (isDirectory) {
                numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
                numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));
            } else { //is not directory
                size = Long.parseLong(parent.getAttribute("size"));
            }

            try {
                creationTime = dateFormat.parse(parent.getAttribute("creationTime"));
                lastAccessTime = dateFormat.parse(parent.getAttribute("lastAccessTime"));
                lastModifiedTime = dateFormat.parse(parent.getAttribute("lastModifiedTime"));
            } catch (ParseException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if (isDiffInfo) { //get isNewlyAccessible isNewlySymbolicLink and state, then decide whether get newSize, newNumberOfFiles, newNumberOfDirectories, newCreatT, newLastAccT, newLastModT, diffInfoChildren
            newString = parent.getAttribute("state");
            //System.out.print(parent.getAttribute("name") + " " + newString + "|");
            if (!newString.isEmpty()) {
                state = ItemState.valueOf(newString.toUpperCase());
            } else {
                state = ItemState.UNMODIFIED; //maybe choose other option
            }
            //System.out.println(" " + state);
            
            newString = parent.getAttribute("newSymbolicLink");
            isNewlySymbolicLink = setNewBoolean(newString, isSymbolicLink);
            
            newString = parent.getAttribute("newAccessible");
            if (!newString.isEmpty()) {
                isNewlyAccessible = newString.equals("true");
                if (!isNewlyAccessible) {
                    isChangedFromAccessibleToUnaccessible = true;
                }
            } else {
                isNewlyAccessible = isAccessible;
            }
            
            if (!isChangedFromAccessibleToUnaccessible) { //get newSize/(newNumberOfFiles, newNumberOfDirectories), newCreatT, newLastAccT, newLastModT, diffInfoChildren
                if (!isDirectory) { //get newSize
                    newString = parent.getAttribute("newSize");
                    if (!newString.isEmpty()) {
                        newSize = Long.parseLong(newString);
                    } else if(state == ItemState.DELETED){
                        newSize = Long.valueOf(0);
                    } else {
                        newSize = size;
                    }
                } else { 
                    newString = parent.getAttribute("newNumberOfFiles");
                    newNumberOfFiles = setNewNumber(newString, state, numberOfFiles);
                    newString = parent.getAttribute("newNumberOfDirectories");
                    newNumberOfDirectories = setNewNumber(newString, state, numberOfDirectories);
                }

                newCreationTime = setDate(parent, creationTime, "newCreationTime", dateFormat);
                newLastAccessTime = setDate(parent, lastAccessTime, "newLastAccessTime", dateFormat);
                newLastModifiedTime = setDate(parent, lastModifiedTime, "newLastModifiedTime", dateFormat);
                newLastModifiedTime = null;
            }
        }
        
        if(isDiffInfo) {
            diffInfoRoot = new DiffInfo();
            diffInfoRoot.setAccessibility(isAccessible);
            diffInfoRoot.setCreationTime(creationTime);
            diffInfoRoot.setDirectory(isDirectory);
            diffInfoRoot.setLastAccessTime(lastAccessTime);
            diffInfoRoot.setLastModifiedTime(lastModifiedTime);
            diffInfoRoot.setName(name);
            diffInfoRoot.setNumberOfDirectories(numberOfDirectories);
            diffInfoRoot.setNumberOfFiles(numberOfFiles);
            diffInfoRoot.setPath(path);
            diffInfoRoot.setSize(size);
            diffInfoRoot.setSymbolicLink(isSymbolicLink);
            
            diffInfoRoot.setState(state);
            diffInfoRoot.setNewAccesibility(isNewlyAccessible); 
            diffInfoRoot.setNewCreationTime(newCreationTime);
            diffInfoRoot.setNewLastAccessTime(newLastAccessTime);
            diffInfoRoot.setNewLastModifiedTime(newLastModifiedTime);
            diffInfoRoot.setNewNumberOfDirectories(newNumberOfDirectories);
            diffInfoRoot.setNewNumberOfFiles(newNumberOfFiles);
            diffInfoRoot.setNewSize(newSize);
            diffInfoRoot.setNewSymbolicLink(isSymbolicLink);
                        
            if (isDirectory) {
                for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                    if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element child = (Element) parent.getChildNodes().item(i);
                        if (diffInfoRoot.getState() == ItemState.CREATED) { //mark children of deleted also as deleted
                            child.setAttribute("state", "created");
                        } 
                        diffInfoRoot.addChild((DiffInfo) childrenToInfo(child, diffInfoRoot.getPath(), isDiffInfo));   
                    }
                }
            }
            
            return (FileInfo) diffInfoRoot;
        } else {
            fileInfoRoot = new FileInfo();
            fileInfoRoot.setAccessibility(isAccessible);
            fileInfoRoot.setCreationTime(creationTime);
            fileInfoRoot.setDirectory(isDirectory);
            fileInfoRoot.setLastAccessTime(lastAccessTime);
            fileInfoRoot.setLastModifiedTime(lastModifiedTime);
            fileInfoRoot.setName(name);
            fileInfoRoot.setNumberOfDirectories(numberOfDirectories);
            fileInfoRoot.setNumberOfFiles(numberOfFiles);
            fileInfoRoot.setPath(path);
            fileInfoRoot.setSize(size);
            fileInfoRoot.setSymbolicLink(isSymbolicLink);

            for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) parent.getChildNodes().item(i);
                    fileInfoRoot.addChild(childrenToInfo(child, fileInfoRoot.getPath(), isDiffInfo));
                }
            }

            return fileInfoRoot;
        }
    }
}
