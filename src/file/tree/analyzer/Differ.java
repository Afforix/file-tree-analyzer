/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import file.tree.analyzer.XMLFileManager;
import java.io.IOException;
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
import org.w3c.dom.Node;

/**
 * Class for finding differences between two XML docs, storing them and
 * converting to FileInfo 
 *
 * @author jindra
 */
public class Differ {

    private Document controlDoc; //newer doc, this doc will stay unchanged
    private Document olderDoc; //current Disc state, this doc will stay unchanged
    private Document testDoc; //testDoc is copy of olderDoc, will be modified and used for FileInfo transformation

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
     * @param cwd current working directory (path to analyses directory)
     * @param controlName name of NEWER XML file
     * @param testName name of OLDER XML file
     * @throws IOException
     * @return DiffInfo (which is extended FileInfo)
     */
    public DiffInfo diffXMLs(String cwd, String controlName, String testName) throws NullPointerException, ConfigurationException {
        try {
            XMLUnit.setCompareUnmatched(false); //TODO - deal with deleted / created items (they are unmatched) 
            //XMLUnit.setIgnoreComments(true); 
            XMLUnit.setIgnoreAttributeOrder(true);
            //XMLUnit.setIgnoreWhitespace(true); //causes bug - why?
        } catch (ConfigurationException ex) {
            Logger.getLogger(Differ.class.getName()).log(Level.SEVERE, "XMLUnit configuration failed.", ex);
        } 

        XMLFileManager fileManager = new XMLFileManager(cwd);

        try {
            controlDoc = fileManager.findXMLFile(controlName, true);
            olderDoc = fileManager.findXMLFile(testName, true);
            testDoc = (Document) olderDoc.cloneNode(true); // for modifying attrs, we don't want to edit source XML olderDoc
        } catch (NullPointerException ex) {
            Logger.getLogger(Differ.class.getName()).log(Level.SEVERE, "Differ: input document not found.", ex);
        }
        
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
        NodeDetail controlNodeDetail = null;
        NodeDetail testNodeDetail = null;
        boolean attributeChange = false;
        boolean createdOrDeleted = false;
        boolean attributeAdded = false;
        
        XPath testXPath = XPathFactory.newInstance().newXPath();
        XPath controlXPath = XPathFactory.newInstance().newXPath();
        Element elementToChange;
        String xpathToElement = null;
        String attributeToChange = null;

        while (listIterator.hasNext()) {
            difference = (Difference) listIterator.next();
            description = difference.getDescription();
            String whatChanged = description.substring(0, description.indexOf(" "));
            controlNodeDetail = difference.getControlNodeDetail();
            testNodeDetail = difference.getTestNodeDetail();

            //TODO: XPATHNODETRACKER
            attributeChange = whatChanged.equals("attribute");
            String testString = difference.getTestNodeDetail().getValue();
            String controlString = difference.getControlNodeDetail().getValue();
            createdOrDeleted = (difference.equals(DifferenceConstants.CHILD_NODE_NOT_FOUND)
                    && (testString.equals("file") || testString.equals("directory") || testString.equals("null"))
                    && (controlString.equals("file") || controlString.equals("directory") || controlString.equals("null")));
            attributeAdded = difference.equals(DifferenceConstants.ATTR_NAME_NOT_FOUND);
            
            if ((attributeChange || createdOrDeleted) && !attributeAdded) {
                try {
                    if (attributeChange) {
                        String elementValue = controlNodeDetail.getValue();
                        xpathToElement = testNodeDetail.getXpathLocation();
                        attributeToChange = xpathToElement.substring(xpathToElement.indexOf('@') + 1); // attr is after @ in xpath
                        String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));
                        
                        Node testNode = (Node) testXPath.evaluate(xpathToParent, testDoc.getDocumentElement(), XPathConstants.NODE);
                         
                        elementToChange = (Element) testNode;
                        elementToChange.setAttribute("state", "modified");
                        elementToChange.setAttribute("new" + attributeToChange.substring(0, 1).toUpperCase() + attributeToChange.substring(1), elementValue);
                        
                    } else if (createdOrDeleted) {

                        if (controlNodeDetail.getValue().equals("null")) { //testNode was deleted
                            xpathToElement = testNodeDetail.getXpathLocation();
                            Node testNode = (Node) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODE);
                            elementToChange = (Element) testNode;
                            elementToChange.setAttribute("state", "deleted");

                        } else if (difference.getTestNodeDetail().getValue().equals("null")) { //testNode is null so controlNode was created - copy from controlDoc to testDoc
                            xpathToElement = controlNodeDetail.getXpathLocation();
                            String xpathToParent = xpathToElement.substring(0, xpathToElement.lastIndexOf("/"));

                            Node controlNode = controlNodeDetail.getNode();
                            Node copyNode = testDoc.importNode(controlNode, false); //importedbut not placed yet
                            Node testNode = (Node) testXPath.evaluate(xpathToParent, testDoc.getDocumentElement(), XPathConstants.NODE);
                            testNode.appendChild(copyNode); //testNode is parent
                            Node copiedNode = (Node) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODE);

                            elementToChange = (Element) copiedNode;
                            elementToChange.setAttribute("state", "created");
                        }
                    }
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(Differ.class.getName()).log(Level.SEVERE, null, ex); //some monster
                }
            }
        }
        
//        XMLFileManager testingFileManager = new XMLFileManager("./saved_analyses"); //FOR TESTING ONLY!!!
//        testingFileManager.createXMLFile(testDoc); // Write to XML - not necessary

        return FileInfoConverter.domToDiffInfo2(testDoc);
    }
}
