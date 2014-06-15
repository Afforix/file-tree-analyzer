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
 * converting to DiffInfo
 *
 * @author jindra
 */
public class Differ {

    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
  
    /**
     * Opens two documents using parameters given and calls diffDocuments 
     * 
     * @throws ConfigurationException
     * @throws NullPointerException
     * @param cwd current working directory (path to analyses directory)
     * @param controlName name of an XML file with newer file tree
     * @param testName name of an XML file with older file tree
     * @return root of file tree with diff information
     * @see diffDocuments
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
     * Compares two documents representing file trees and returns data structure
     * with comparison information. Both given documents will remain unchanged.
     * Uses XMLUnit engine (http://xmlunit.sourceforge.net/)
     *
     * @param testDoc document representing older file tree
     * @param newDoc document representing newer file tree
     * @return root of file tree with diff information
     */
    public static DiffInfo diffDocuments(Document newDoc, Document testDoc) throws ConfigurationException {
        try { //set XMLUnit
            XMLUnit.setCompareUnmatched(false);
            XMLUnit.setIgnoreAttributeOrder(true);
            XMLUnit.setIgnoreWhitespace(true);
        } catch (ConfigurationException ex) {
            logger.log(Level.SEVERE, "XMLUnit configuration failed.", ex);
        }

        Document controlDoc = (Document) newDoc.cloneNode(true);

        Diff diff = new Diff(controlDoc, testDoc);
        //name of element + attribute "name" creates ID
        String attributeUsedAsID = "name";
        ElementNameAndAttributeQualifier elementNameAndAttributeQualifier = new ElementNameAndAttributeQualifier(attributeUsedAsID);
        diff.overrideElementQualifier(elementNameAndAttributeQualifier);

        DetailedDiff detailedDiff = new DetailedDiff(diff);
        detailedDiff.overrideElementQualifier(elementNameAndAttributeQualifier);
        List allDifferences = detailedDiff.getAllDifferences();
        ListIterator listIterator = allDifferences.listIterator();

        Difference difference; //one item from allDifferences List
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

        while (listIterator.hasNext()) { //solve every item in list of differences
            difference = (Difference) listIterator.next();
            description = difference.getDescription();
            String whatChanged = description.substring(0, description.indexOf(" "));
            testNodeDetail = difference.getTestNodeDetail();
            controlNodeDetail = difference.getControlNodeDetail();

            String testValue = testNodeDetail.getValue();
            String controlValue = controlNodeDetail.getValue();

            //recognize what type of difference do we have
            attributeChange = whatChanged.equals("attribute");
            createdOrDeleted = (difference.equals(DifferenceConstants.CHILD_NODE_NOT_FOUND)
                    && (((testValue.equals("file") || testValue.equals("directory")) && controlValue.equals("null"))
                    || ((controlValue.equals("file") || controlValue.equals("directory")) && testValue.equals("null"))));
            attributeAdded = difference.equals(DifferenceConstants.ATTR_NAME_NOT_FOUND);

            if (!attributeAdded) {
                try {
                    if (attributeChange) { //node was modified - mark it, add new* attribute
                        xpathToElement = controlNodeDetail.getXpathLocation();
                        attributeToChange = xpathToElement.substring(xpathToElement.indexOf('@') + 1); // attr is after @ in xpath
                        String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));

                        Node node = (Node) controlXPath.evaluate(xpathToParent, controlDoc.getDocumentElement(), XPathConstants.NODE);
                        elementToChange = (Element) node;
                        elementToChange.setAttribute("state", "modified");
                        elementToChange.setAttribute("new" + attributeToChange.substring(0, 1).toUpperCase() + attributeToChange.substring(1), controlValue);
                        elementToChange.setAttribute(attributeToChange, testValue);

                    } else if (createdOrDeleted) {
                        if (testValue.equals("null")) { //node was created - just mark it
                            xpathToElement = controlNodeDetail.getXpathLocation();
                            Node node = (Node) controlXPath.evaluate(xpathToElement, controlDoc.getDocumentElement(), XPathConstants.NODE);
                            elementToChange = (Element) node;
                            elementToChange.setAttribute("state", "created");

                        } else if (controlValue.equals("null")) { //node was deleted - copy from testDoc to controlDoc to proper place
                            xpathToElement = testNodeDetail.getXpathLocation();
                            String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));

                            Node testNode = testNodeDetail.getNode();
                            Element testElement = (Element) testNode;
                            String testNodeName = testElement.getAttribute("name");

                            Node importNode = controlDoc.importNode(testNode, true); //importNode does not place it yet!; true means with children
                            Element importElement = (Element) importNode;
                            importElement.setAttribute("state", "deleted");

                            //get names of node ancestors (whole path from root node)
                            String fullName = ""; //for example /myDirAlpha/myDirBeta
                            Node testNodeAncestor;
                            String xpathToAncestor = xpathToParent;
                            Element testElementAncestor;
                            String testElementAncestorName = "";

                            while (true) {
                                if (!xpathToAncestor.contains("/")) {
                                    break;
                                }
                                testNodeAncestor = (Node) testXPath.evaluate(xpathToAncestor, testDoc.getDocumentElement(), XPathConstants.NODE);
                                testElementAncestor = (Element) testNodeAncestor;
                                testElementAncestorName = testElementAncestor.getAttribute("name");
                                fullName = testElementAncestorName + "/" + fullName;
                                xpathToAncestor = xpathToAncestor.substring(0, xpathToAncestor.lastIndexOf("/"));
                            }

                            //get the right path in controlDoc
                            //first get names of nodes on certain "level" (they are siblings)
                            //then compare them to name from fullName (on the same level)
                            //choose the one whose name equals, repeat procedure on his children
                            //when finished, result is path in controlDoc where should be importNode placed
                            String[] names = fullName.split("/"); //for example /directory[2] 
                            String[] levels = xpathToParent.split("/"); //for example myLittleDir
                            Node controlNodeAncestor;
                            NodeList controlSiblingsList;
                            Node currentControlNode; //child of controlNodeAncestor
                            Element currentControlElement;
                            String currentControlElementName;
                            String path = "/" + levels[1]; // /directory[1]
                            String pompath = "";
                            int realDirectoryNumb = 0;

                            for (int i = 1; i < names.length; i++) {
                                controlNodeAncestor = (Node) controlXPath.evaluate(path, controlDoc.getDocumentElement(), XPathConstants.NODE);
                                controlSiblingsList = controlNodeAncestor.getChildNodes();
                                realDirectoryNumb = 0;

                                for (int j = 0; j < controlSiblingsList.getLength(); j++) {
                                    if (controlSiblingsList.item(j).getNodeName().equals("directory")) { //do not check "file" nodes
                                        realDirectoryNumb++; //use this instead of j because in controlSiblingsList may be file before directory
                                        pompath = path; //backup path
                                        path = path + "/directory[" + realDirectoryNumb + "]"; //for example /directory[1]/directory[3]

                                        currentControlNode = (Node) controlXPath.evaluate(path, controlDoc.getDocumentElement(), XPathConstants.NODE);
                                        currentControlElement = (Element) currentControlNode;
                                        currentControlElementName = currentControlElement.getAttribute("name");
                                        if (currentControlElementName.equals(names[i])) { //bingo!
                                            break; //path is longer now so break and process "lower level"
                                        } else {
                                            path = pompath; //restore path
                                        }
                                    }
                                }
                            }

                            currentControlNode = (Node) controlXPath.evaluate(path, controlDoc.getDocumentElement(), XPathConstants.NODE); //where to append

                            if (!currentControlNode.hasChildNodes()) { 
                                currentControlNode.appendChild(importNode); //simple
                                
                            } else { //find node in siblings before which should be importNode placed
                                controlSiblingsList = currentControlNode.getChildNodes(); //future siblings of importNode
                                Node controlNextSibling = null;
                                String controlNextSiblingName = "";
                                boolean inserted = false;
                                int compare;

                                //find where to place importNode - after alphabetically smaller names and before greater names
                                //place file after all folders
                                for (int i = 0; i < controlSiblingsList.getLength(); i++) {
                                    controlNextSibling = controlSiblingsList.item(i);
                                    if (controlNextSibling.getNodeName().equals("file") || controlNextSibling.getNodeName().equals("directory")) {
                                        NamedNodeMap nnm = controlNextSibling.getAttributes(); //find out name of current file/directory
                                        controlNextSiblingName = nnm.getNamedItem("name").getNodeValue();

                                        compare = String.CASE_INSENSITIVE_ORDER.compare(testNodeName, controlNextSiblingName);
                                        if ((compare < 0) && !(controlNextSibling.getNodeName().equals("directory") && importNode.getNodeName().equals("file"))) { //avoid placing file before directory
                                            currentControlNode.insertBefore(importNode, controlNextSibling);
                                            inserted = true;
                                            break;
                                        } else if (controlNextSibling.getNodeName().equals("file") && importNode.getNodeName().equals("directory")) { //place directory before files
                                            currentControlNode.insertBefore(importNode, controlSiblingsList.item(i));
                                            inserted = true;
                                            break;
                                        }
                                    }
                                }
                                if (!inserted) { //importNode will be alphbeabetically greatest child of currentControlNode
                                    currentControlNode.appendChild(importNode);
                                }
                            }
                            
                            /*
                            if (testValue.equals("directory")) { //append children (files and directories) to imported node - currently done by "true" in importNode()
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
                            */
                        }
                    }
                } catch (XPathExpressionException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        //XMLFileManager controlingFileManager = new XMLFileManager("./saved_analyses"); //for testing only
        //controlingFileManager.createXMLFile(controlDoc); // for testing only
        return FileInfoConverter.domToDiffInfo(controlDoc); //return diffInfo type
    }
}
