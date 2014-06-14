/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.NodeDetail;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for finding differences between two XML docs, storing them and
 * converting to FileInfo
 *
 * @author jindra
 */
public class Differ {

    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
   //main is for testing purpose only
    /*
     public static void main(String[] args) {
     Differ differ = new Differ();
     DiffInfo diffInfo;
     try {
     diffInfo = differ.diffXMLs("./test/file/tree/analyzer", "controlXMLForDiffer.xml", "testXMLForDiffer.xml");
     printIt(diffInfo, 0);
     } catch (IllegalArgumentException e) {
     System.out.println("error in main" + e);
     }
     }

     //print DiffInfo for testing only
     private static void printIt(DiffInfo parent, int level) {
     System.out.println("NAME:" + parent.getName()
     + " path:" + parent.getPath()
     + " directory:" + parent.isDirectory()
     + " symlink:" + parent.isSymbolicLink()
     + " accesible: " + parent.isAccessible()
     + " state: " + parent.getState()
     );

     if (parent.isAccessible() || parent.isNewlyAccessible()) {
     if (parent.isDirectory()) {
     System.out.println("numberOfFiles:" + parent.getNumberOfFiles() + " new:" + parent.getNewNumberOfFiles());                        
     System.out.println("numberOfDirectories:" + parent.getNumberOfDirectories() + " new:" + parent.getNewNumberOfDirectories());
     } else {
     System.out.println("size:" + parent.getSize() + " new:" + parent.getNewSize());
     }
     //System.out.println(" creationTime:" + parent.getCreationTime()
     //        + " lastAccessTime:" + parent.getLastAccessTime()
     //        + " lastModifiedTime:" + parent.getLastModifiedTime()
     //);
     //if (!parent.getCreationTime().toString().isEmpty()) {
     //   System.out.println("creationTime:" + parent.getCreationTime() + " new:" + parent.getNewCreationTime());
     //}
     //if (!parent.getNewLastAccessTime().toString().isEmpty()) {
     //    System.out.println("lastAccessTime:" + parent.getLastAccessTime() + " new:" + parent.getNewLastAccessTime());
     //
     //if (!parent.getNewLastModifiedTime().toString().isEmpty()) {
     //    System.out.println("lastModifiedTime:" + parent.getLastModifiedTime() + " new:" + parent.getNewLastModifiedTime());
     //}

     //System.out.println(" newSize:" + parent.getNewSize()
     //        + " newCreationTime:" + parent.getNewCreationTime()
     //        + " newLastAccessTime:" + parent.getNewLastAccessTime()
     //        + " newLastModifiedTime:" + parent.getNewLastModifiedTime()
     //        + " ||| "
     //);
     }
        
     if(parent.isDirectory() && (parent.isNewlyAccessible() || parent.isAccessible())){
     List<DiffInfo> children = parent.getDiffChildren();
     for (DiffInfo file : children) {
     for (int i = 0; i <= level; i++) {
     System.out.print(" ");
     }
     printIt((DiffInfo) file, level + 1);
     }
     }
     }
     */

    /**
     * Finds differences between two two XML docs, stores differences in third
     * DOM
     *
     * @throws ConfigurationException
     * @throws NullPointerException
     * @param cwd current working directory (path to analyses directory)
     * @param controlName name of an XML file with newer file tree
     * @param testName name of an XML file with older file tree
     * @return root of file tree with diff information
     */
    public static DiffInfo diffXMLs(String cwd, String controlName, String testName) throws NullPointerException {
        XMLFileManager fileManager = new XMLFileManager(cwd);
        Document testDoc;
        Document newDoc;

        testDoc = fileManager.findXMLFile(testName, true);
        newDoc = fileManager.findXMLFile(controlName, true);

        if (newDoc == null || testDoc == null) {
            throw new NullPointerException("failed to load XML file");
        }
        DiffInfo ret = diffDocuments(testDoc, newDoc);
        logger.log(Level.INFO, "diffed " + testName + " and " + controlName);
        return ret;
    }

    /**
     * Compares two documents representing file trees with the same root and
     * returns data structure with comparison information. Both given documents
     * will remain unchanged.
     *
     * @param testDoc document representing older file tree
     * @param newDoc document representing newer file tree
     * @return root of file tree with diff information
     */
    public static DiffInfo diffDocuments(Document newDoc, Document testDoc) throws ConfigurationException {
        try {
            XMLUnit.setCompareUnmatched(false); 
            XMLUnit.setIgnoreAttributeOrder(true);
            //XMLUnit.setIgnoreWhitespace(true); //causes bug - why?
        } catch (ConfigurationException ex) {
           logger.log(Level.SEVERE, "XMLUnit configuration failed.", ex);
        }

        Document controlDoc = (Document) newDoc.cloneNode(true);

        Diff diff = new Diff(controlDoc, testDoc);
        //name of element + attribute "name" is ID
        String attributeUsedAsID = "name";
        ElementNameAndAttributeQualifier elementNameAndAttributeQualifier = new ElementNameAndAttributeQualifier(attributeUsedAsID);
        diff.overrideElementQualifier(elementNameAndAttributeQualifier);

        DetailedDiff detailedDiff = new DetailedDiff(diff);
        detailedDiff.overrideElementQualifier(elementNameAndAttributeQualifier);
        List allDifferences = detailedDiff.getAllDifferences();
        ListIterator listIterator = allDifferences.listIterator();

        Difference difference; //one item from allDifferences List
        String modifiedElement = null; //path to modified element
        String description = null;
        NodeDetail testNodeDetail = null;
        NodeDetail controlNodeDetail = null;
        boolean attributeChange = false;
        boolean createdOrDeleted = false;
        boolean attributeAdded = false;

        XPath testXPath = XPathFactory.newInstance().newXPath();
        XPath controlXPath = XPathFactory.newInstance().newXPath();
        Element elementToChange = null;
        String xpathToElement = null;
        String attributeToChange = null;

        String testRootPath = testDoc.getDocumentElement().getAttribute("path");
        String controlRootPath = controlDoc.getDocumentElement().getAttribute("path");
        System.out.println("testRootPath: " + testRootPath + " controlRootPath: " + controlRootPath);

        while (listIterator.hasNext()) {
            difference = (Difference) listIterator.next();
            description = difference.getDescription();
            String whatChanged = description.substring(0, description.indexOf(" "));
            testNodeDetail = difference.getTestNodeDetail();
            controlNodeDetail = difference.getControlNodeDetail();
            
            //TODO: XPATHNODETRACKER
            attributeChange = whatChanged.equals("attribute");
            String testValue = testNodeDetail.getValue();
            String controlValue = controlNodeDetail.getValue();

            //System.out.println(testValue + " " + controlValue);
            createdOrDeleted = (difference.equals(DifferenceConstants.CHILD_NODE_NOT_FOUND)
                    && (((testValue.equals("file") || testValue.equals("directory")) && controlValue.equals("null"))
                    || ((controlValue.equals("file") || controlValue.equals("directory")) && testValue.equals("null"))));

            attributeAdded = difference.equals(DifferenceConstants.ATTR_NAME_NOT_FOUND);

            if (!attributeAdded) {
                try {
                    if (attributeChange) {
                        xpathToElement = controlNodeDetail.getXpathLocation();
                        attributeToChange = xpathToElement.substring(xpathToElement.indexOf('@') + 1); // attr is after @ in xpath
                        String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));

                        Node node = (Node) controlXPath.evaluate(xpathToParent, controlDoc.getDocumentElement(), XPathConstants.NODE);

                        elementToChange = (Element) node;
                        elementToChange.setAttribute("state", "modified");
                        elementToChange.setAttribute("new" + attributeToChange.substring(0, 1).toUpperCase() + attributeToChange.substring(1), testValue);

                    } else if (createdOrDeleted) {

                        if (testValue.equals("null")) { //node was created
                            xpathToElement = controlNodeDetail.getXpathLocation();
                            
                            Node node = (Node) controlXPath.evaluate(xpathToElement, controlDoc.getDocumentElement(), XPathConstants.NODE);
                            elementToChange = (Element) node;
                            elementToChange.setAttribute("state", "created");

                        } else if (controlValue.equals("null")) { //node was deleted - copy from testDoc controlDoc
                            xpathToElement = testNodeDetail.getXpathLocation();
                            String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));
                          
                            Node testNode = testNodeDetail.getNode();
                            Element testElement = (Element) testNode;
                            String testNodeName = testElement.getAttribute("name");
                            
                            Node importNode = controlDoc.importNode(testNode, true);
                            Element importElement = (Element) importNode;
                            importElement.setAttribute("state", "deleted");
                            
                            Node controlNodeParent = (Node) controlXPath.evaluate(xpathToParent, controlDoc.getDocumentElement(), XPathConstants.NODE); //where to append
                            
                            if (!controlNodeParent.hasChildNodes()) {
                                controlNodeParent.appendChild(importNode);
                            } else {
                                NodeList controlChildren = controlNodeParent.getChildNodes(); //future siblings of importNode
                                Node controlNextSibling = null;
                                String controlNextSiblingName = "";

                            //find where to place importNode - after alphabetically smaller names and before greater names
                                //place file after all folders
                                for (int i = 0; i < controlChildren.getLength(); i++) {
                                    controlNextSibling = controlChildren.item(i);
                                    if (controlNextSibling.getNodeName().equals("file") || controlNextSibling.getNodeName().equals("directory")) {
                                        NamedNodeMap nnm = controlNextSibling.getAttributes(); //find out name of current file/directory
                                        controlNextSiblingName = nnm.getNamedItem("name").getNodeValue();
                                        int compare = String.CASE_INSENSITIVE_ORDER.compare(testNodeName, controlNextSiblingName);
                                        if ((compare < 0) //greater than zero means controlNextSiblingName is greater
                                                && !(controlNextSibling.getNodeName().equals("directory") && importNode.getNodeName().equals("file"))) { //avoid placing file before directory
                                            //controlNodeParent.insertBefore(importNode, controlNextSibling); //place importNode before controlNextSibling 
                                            //controlNodeParent.insertBefore(importNode, controlChildren.item(i - 2)); //-2 because then it does not fall - but still does not place importNode properly!!!
                                            break;
                                        } else if (controlNextSibling.getNodeName().equals("file") && importNode.getNodeName().equals("directory")) { //place directory before files
                                            //controlNodeParent.insertBefore(importNode, controlChildren.item(i - 2));
                                        }
                                    }
                                }
                            }

                            
                            if (testValue.equals("directory")) { //append children (files and directories) to imported node - currently done by ChildrenToInfo
                                Node node = (Node) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODE);
                                NodeList nodeList = node.getChildNodes();
                                Node controlNode = (Node) controlXPath.evaluate(xpathToElement, controlDoc.getDocumentElement(), XPathConstants.NODE);
                                
                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    testNodeName = nodeList.item(i).getNodeName();
                                    if (testNodeName.equals("file") || testNodeName.equals("directory")) {
                                        importNode = controlDoc.importNode(nodeList.item(i), false);
                                        importElement = (Element) importNode;
                                        importElement.setAttribute("state", "deleted");
                                        
                                        controlNode.appendChild(importNode);
                                    }
                                }
                            }                                     
                        }
                    }
                } catch (XPathExpressionException ex) {
                   logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        //XMLFileManager controlingFileManager = new XMLFileManager("./saved_analyses"); //FOR TESTING ONLY!!!
        //controlingFileManager.createXMLFile(controlDoc); // Write to XML - not necessary
        return FileInfoConverter.domToDiffInfo(controlDoc);
    }
}
