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
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
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
     */
    public static FileInfo domToFileInfo(Document doc) {
        Element rootElement = doc.getDocumentElement();
        FileInfo root = childrenToFileInfo(rootElement, "");

        return root;
    }

    private static FileInfo childrenToFileInfo(Element element, String path) {
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
            item.setNumberofDirectories(Integer.parseInt(element.getAttribute("numberOfDirectories")));

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

        item.setSymbolicLink(Boolean.parseBoolean(element.getAttribute("isSymbolicLink")));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        try {
            item.setCreationTime(dateFormat.parse(element.getAttribute("creationTime")));
            item.setLastAccessTime(dateFormat.parse(element.getAttribute("lastAccessTime")));
            item.setLastModifiedTime(dateFormat.parse(element.getAttribute("lastModifiedTime")));
        } catch (ParseException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return item;
    }

    /**
     * Takes object representing XML DOM and converts it to DiffInfo.
     *
     * @param doc XML Dom
     * @return FileInfo with DiffInfo
     * @deprecated
     */
    public static DiffInfo domToDiffInfo(Document doc) {
        Element rootElement = doc.getDocumentElement();
        DiffInfo root = childrenToDiffInfo(rootElement, "");

        return root;
    }

    /**
     * @deprecated
     */
    private static DiffInfo childrenToDiffInfo(Element parent, String path) {
        String name = "";
        boolean isDirectory = false;
        List<FileInfo> fileInfoChildren = null;
        List<DiffInfo> diffInfoChildren = null;
        Long size = null;
        Long newSize = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;

        //DiffInfo lines - this is different from childrenToFileInfo
        String newString = ""; //temporary variable, not part of DiffInfo
        ItemState state = null;
        //end

        //added to enable program run - not safe, untested
        boolean isAccessible = parent.getAttribute("Accessibility").equals("true");
        //end

        name = parent.getAttribute("name");
        //System.out.println("NAME:" + name);

        if (parent.getTagName().equals("directory")) {
            isDirectory = true;
            numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
            numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));

            if (parent.hasAttribute("path")) {
                path = parent.getAttribute("path");
            } else {
                path += "/" + name;
            }

            diffInfoChildren = new ArrayList<>();
            for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                //DiffInfo modification - added if()
                if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) parent.getChildNodes().item(i);
                    diffInfoChildren.add(childrenToDiffInfo(child, path));
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
        //System.out.println("PATH:" + path);

        boolean isSymbolicLink = false;
        if (parent.getAttribute("isSymbolicLink").equals("true")) {
            isSymbolicLink = true;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
        //System.out.println("*Time:");
        try {
            creationTime = dateFormat.parse(parent.getAttribute("creationTime"));
            //System.out.println("CREATIONTIME:" + creationTime);
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
            state = ItemState.CREATED; //maybe choose other option
        }
        //end

        //DiffInfo lines, create new DiffInfo instead of new FileInfo
        DiffInfo root = new DiffInfo(name, path, isDirectory, isSymbolicLink, isAccessible, size, creationTime,
                lastAccessTime, lastModifiedTime, fileInfoChildren, numberOfFiles, numberOfDirectories,
                state, diffInfoChildren, newSize, newCreationTime, newLastAccessTime, newLastModifiedTime);
        //end

        return root; //DiffInfo type
    }

    /**
     * Gets date from desired attribute or uses "older" attribute.
     *
     * @param parent element whose attribute should be tested
     * @param oldAttribute already existing Date (for example creationTime)
     * @param newAttributeName name of Date to set (for example newCreationTime)
     * @param dateFormat how should output look like
     * @return date
     */
    public static Date setDate(Element parent, Date oldAttribute, String newAttributeName, DateFormat dateFormat) {
        Date date = null;
        String newAttribute = parent.getAttribute(newAttributeName);

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

    //ALL FOLLOWING IS SUGGESTION
    /**
     * Takes object representing XML DOM and converts it to FileInfo.
     *
     * @param doc XML Dom
     * @return FileInfo
     */
    //if accepted, change name to domToFIleInfo
    public static FileInfo domToFileInfo2(Document doc) {
        Element rootElement = doc.getDocumentElement();
        FileInfo root = (FileInfo) childrenToInfo(rootElement, "", false);

        return root;
    }

    /**
     * Takes object representing XML DOM and converts it to DiffInfo.
     *
     * @param doc XML Dom
     * @return FileInfo with DiffInfo
     */
    //if accepted, change name to domToDiffInfo
    public static DiffInfo domToDiffInfo2(Document doc) {
        Element rootElement = doc.getDocumentElement();
        DiffInfo root = (DiffInfo) childrenToInfo(rootElement, "", true);

        return root;
    }

    //should be used by both domToFileInfo and domToDiffInfo
    /**
     *
     * @param parent Element to be processed
     * @param path To parent element
     * @param isDiffInfo DiffInfo needs additional code
     * @return Object. This can be casted back to FileInfo or DiffInfo
     */
    private static Object childrenToInfo(Element parent, String path, boolean isDiffInfo) {
        String name = "";
        boolean isDirectory = false;
        boolean isAccessible = false;
        boolean isSymbolicLink = false;

        Long size = null;
        Long newSize = null;
        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        ItemState state = null; //for DiffInfo only

        Date creationTime = null;
        Date lastAccessTime = null;
        Date lastModifiedTime = null;
        Date newCreationTime = null;
        Date newLastAccessTime = null;
        Date newLastModifiedTime = null;

        List<FileInfo> fileInfoChildren = null;
        List<DiffInfo> diffInfoChildren = null;

        String newString = "";  //temporary variable

        name = parent.getAttribute("name");
        isDirectory = parent.getTagName().equals("directory");
        isSymbolicLink = parent.getAttribute("isSymbolicLink").equals("true");

        if (isDirectory && parent.hasAttribute("path")) {
            path = parent.getAttribute("path");
        } else {
            path += "/" + name;
        }

        isAccessible = parent.getAttribute("accessibility").equals("true");
        if (isAccessible) {
            if (isDirectory) {
                numberOfFiles = Integer.parseInt(parent.getAttribute("numberOfFiles"));
                numberOfDirectories = Integer.parseInt(parent.getAttribute("numberOfDirectories"));

                if (isDiffInfo) {
                    diffInfoChildren = new ArrayList<>();
                } else {
                    fileInfoChildren = new ArrayList<>();
                }

                for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
                    if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element child = (Element) parent.getChildNodes().item(i);
                        if (isDiffInfo) {
                            diffInfoChildren.add((DiffInfo) childrenToInfo(child, path, true)); //casting type to avoid "Object type" files 
                        } else {
                            fileInfoChildren.add((FileInfo) childrenToInfo(child, path, false));
                        }
                    }
                }
            } else { //is not directory
                size = Long.parseLong(parent.getAttribute("size"));

                if (isDiffInfo) {
                    newString = parent.getAttribute("newSize");
                    if (!newString.isEmpty()) {
                        newSize = Long.parseLong(newString); //else remain null
                    }
                }
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            try {
                creationTime = dateFormat.parse(parent.getAttribute("creationTime"));
                lastAccessTime = dateFormat.parse(parent.getAttribute("lastAccessTime"));
                lastModifiedTime = dateFormat.parse(parent.getAttribute("lastModifiedTime"));
            } catch (ParseException ex) {
                Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (isDiffInfo) {
                newCreationTime = setDate(parent, creationTime, "newCreationTime", dateFormat);
                newLastAccessTime = setDate(parent, lastAccessTime, "newLastAccessTime", dateFormat);
                newLastModifiedTime = setDate(parent, lastModifiedTime, "newLastModifiedTime", dateFormat);

                newString = parent.getAttribute("state");
                //System.out.println("STATE: " + newString);
                if (!newString.isEmpty()) {
                    state = ItemState.valueOf(parent.getAttribute("state").toUpperCase());
                    //state = ItemState.valueOf(parent.getAttribute("state"));
                } else {
                    state = ItemState.UNMODIFIED; //maybe choose other option
                }
            }
        }

        if (isDiffInfo) {
            DiffInfo root = new DiffInfo(name, path, isDirectory, isSymbolicLink, isAccessible, size, creationTime,
                    lastAccessTime, lastModifiedTime, fileInfoChildren, numberOfFiles, numberOfDirectories,
                    state, diffInfoChildren, newSize, newCreationTime, newLastAccessTime, newLastModifiedTime);

            return (Object) root;
        } else {
            FileInfo root = new FileInfo(name, path, isDirectory, isSymbolicLink, isAccessible, size, creationTime,
                    lastAccessTime, lastModifiedTime, fileInfoChildren, numberOfFiles, numberOfDirectories);
            return (Object) root;
        }
    }
}
