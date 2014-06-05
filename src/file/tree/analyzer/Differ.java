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
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.NodeDetail;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.custommonkey.xmlunit.exceptions.XMLUnitException;
import org.custommonkey.xmlunit.exceptions.XMLUnitRuntimeException;
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

    private Document controlDoc; //newer doc, this doc will stay unchanged
    private Document olderDoc; //current Disc state, this doc will stay unchanged
    private Document testDoc; //testDoc is copy of olderDoc, will be modified and used for FileInfo transformation

    //main is for testing purpose only
    
    public static void main(String[] args) {
        Differ differ = new Differ();
        DiffInfo diffInfo;
        try {
            System.out.println("wombat");
            diffInfo = differ.diffXMLs("./saved_analyses", "2014-05-24T222222.xml", "2014-05-24T111111.xml");
            printIt(diffInfo, 0);
        } catch (IOException e) {
            System.out.println("error in main");
        }
    }
    
    //print DiffInfo for testing only
    private static void printIt(DiffInfo parent, int level) {
        System.out.println("name:" + parent.getName() + 
                " directory:" + parent.isDirectory() + 
                //" symlink:" + parent.isSymbolicLink() + 
                //" accesible: " + parent.isAccessible() +
                //" size:" + parent.getSize() + 
                //" creationTime:" + parent.getCreationTime() + 
                //" lastAccessTime:" + parent.getLastAccessTime() + 
                //" lastModifiedTime:" + parent.getLastModifiedTime() + 
                //" numberOfFiles:" + parent.getNumberOfFiles() + 
                //" numberOfDirectories:"  + parent.getNumberOfDirectories() + 
                //" path:" + parent.getPath() + 
                " ||| newSize:" + parent.getNewSize() + 
                " newCreationTime:" + parent.getNewCreationTime() +  
                " newLastAccessTime:" + parent.getNewLastAccessTime() + 
                " newLastModifiedTime:" + parent.getNewLastModifiedTime() +
                " state: " + parent.getState());
        
        if(parent.isDirectory()){
            List<DiffInfo> children = parent.getDiffChildren();
            for (DiffInfo file : children) {
                for (int i = 0; i <= level; i++) {
                    System.out.print(" ");
                }
                printIt((DiffInfo) file, level + 1);
            }
        }
    }
    

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
    public DiffInfo diffXMLs(String cwd, String controlName, String testName) throws IOException, IllegalArgumentException, ConfigurationException {
        try {
            XMLUnit.setCompareUnmatched(false); //TODO - deal with deleted / created items (they are unmatched) 
            XMLUnit.setIgnoreComments(true); 
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
            System.out.println("controlDoc: " + olderDoc.getBaseURI() + " name: " + olderDoc.getLocalName());
            System.out.println("olderDoc: " + olderDoc.getDocumentURI() + " name: " + olderDoc.getLocalName());
            System.out.println("testDoc: " + testDoc.getDocumentURI() + " name: " + testDoc.getLocalName());
        } catch (IllegalArgumentException ex) {
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
        Element elementToChange;        
        String modifiedElement = null; //path to modified element
        String description = null;
        NodeDetail controlNodeDetail = null;
        NodeDetail testNodeDetail = null;
        boolean attributeChange = false;
        boolean createdOrDeleted = false;
        
        while (listIterator.hasNext()) {
            difference = (Difference) listIterator.next();
            //System.out.println(difference);
            description = difference.getDescription();
            System.out.println(description);
            String whatChanged = description.substring(0, description.indexOf(" "));
            controlNodeDetail = difference.getControlNodeDetail();
            testNodeDetail = difference.getTestNodeDetail();
            
            //TODO: XPATHNODETRACKER
            attributeChange = whatChanged.equals("attribute");
            createdOrDeleted = (difference.equals(DifferenceConstants.CHILD_NODE_NOT_FOUND)
                    && (difference.getTestNodeDetail().getValue().equals("file") || difference.getTestNodeDetail().getValue().equals("directory") || difference.getTestNodeDetail().getValue().equals("null"))
                    && (difference.getControlNodeDetail().getValue().equals("file") || difference.getControlNodeDetail().getValue().equals("directory") || difference.getControlNodeDetail().getValue().equals("null")));

            if (attributeChange || createdOrDeleted) {
                if(createdOrDeleted) {
                    System.out.println("CREATED OR DELETED: " + difference);
                    System.out.println(":: EXPECTED " + difference.getTestNodeDetail().getValue() + " FOUND " + difference.getControlNodeDetail().getValue());
                }
                
                // Get what has changed
                String xpathToAttr = controlNodeDetail.getXpathLocation();
                String newValue = controlNodeDetail.getValue();
                String oldValue = testNodeDetail.getValue();
                
                XPath testXPath = XPathFactory.newInstance().newXPath();
                XPath controlXPath = XPathFactory.newInstance().newXPath();
                try {
                    //TODO: find out how to cast Node to Element, then avoid next line
                    //and create elementToChange using testNodeDetail.getNode()
                    //like Element elementToChange = (Element) testNodeDetail.getNode();
                     //NodeList ControlNodes = (NodeList) controlXPath.evaluate(xpathToControlElem, null)

                    //TODO: possible node states: unmodified,modified,created,deleted
                   
                   /*
                   NodeList controlNodes = (NodeList) controlXPath.evaluate(xpathToElem, controlDoc.getDocumentElement(), XPathConstants.NODESET);
                   if(controlNodes.getLength() == 0){
                       elementToChange.setAttribute("itemState", "created");                       
                   }*/
                    if(attributeChange) {
                        String xpathToElem = xpathToAttr.substring(0, xpathToAttr.indexOf("@") - 1); // /document[1]/@attr                
                        String attr = xpathToAttr.substring(xpathToAttr.indexOf('@') + 1); // attr is after @ in xpath
                        NodeList testNodes = (NodeList) testXPath.evaluate(xpathToElem, testDoc.getDocumentElement(), XPathConstants.NODESET);
                        elementToChange = (Element) testNodes.item(0);
                        elementToChange.setAttribute("new" + attr.substring(0, 1).toUpperCase() + attr.substring(1), newValue);
                        System.out.println("new" + attr.substring(0, 1).toUpperCase() + attr.substring(1) + " <- " + newValue);

                        if (!xpathToAttr.equals(modifiedElement)) {
                            modifiedElement = xpathToAttr;
                            elementToChange.setAttribute("state", "modified");
                            //System.out.println("ITEMSTATE CHANGED");
                        }
                    } else if (createdOrDeleted) {
                        if (difference.getTestNodeDetail().getValue().equals("null")) { //testNode is null so controlNode was created
                            //copy from controlDoc to testDoc
                            String xpathToElement = difference.getControlNodeDetail().getXpathLocation();
                            NodeList controlNodes = (NodeList) controlXPath.evaluate(xpathToElement, controlDoc.getDocumentElement(), XPathConstants.NODESET);
                            //Element el = (Element) controlNodes.item(0);
                            Node nodeToCopy = difference.getControlNodeDetail().getNode();
                            //Node copiedNode = nodeToCopy.cloneNode(false);
                            Node copiedNode = testDoc.importNode(nodeToCopy, false);
                            String xpathToParent = xpathToAttr.substring(0, xpathToElement.lastIndexOf("/"));
                            
                            Node testNodes = (Node) testXPath.evaluate(xpathToParent, testDoc.getDocumentElement(), XPathConstants.NODE);
                            testNodes.appendChild(copiedNode);
                            
                            testNodes = (Node) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODE);
                            elementToChange = (Element) testNodes;
                            elementToChange.setAttribute("state", "created");

                            //elementToChange = (Element) testNodes.item(0);
                            //elementToChange.appendChild(copyNode);
                            /*
                            NodeList testNodes = (NodeList) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODESET);
                            elementToChange = (Element) testNodes.item(0);
                            elementToChange.setAttribute("state", "created");
                            */
                            //Node parent = copyNode.getParentNode();
                            //NodeList testNodes = (NodeList) testXPath.evaluate(xpathToTest, testDoc.getDocumentElement(), XPathConstants.NODESET);
                            
                            //Node nn = testDoc.importNode(difference.getControlNodeDetail().getNode(), false);
                            
                            //System.out.println("NODE: " + xpathToElement+" "+nn.getAttributes().item(0) + nn.getParentNode());
                            //testDoc.(nodeToChange.)
                            //NodeList testNodes = (NodeList) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODESET);
                            //elementToChange = (Element) nodeToChange;
                            //elementToChange.getParentNode().appendChild(copyNode);
                            //System.out.println("X X X: " + xpathToAttr + " || " + elementToChange + " " );

                            
                        } else if (difference.getControlNodeDetail().getValue().equals("null")) { //testNode was deleted
                            //NodeList testNode = (NodeList) testXPath.evaluate(testNodeDetail.getXpathLocation(), testDoc.getDocumentElement(), XPathConstants.NODESET);
                            //elementToChange = (Element) testNode.item(0);
                            //System.out.println("XXX " + difference.getTestNodeDetail().getNode());
                            String xpathToElement = difference.getTestNodeDetail().getXpathLocation();
                            NodeList testNodes = (NodeList) testXPath.evaluate(xpathToElement, testDoc.getDocumentElement(), XPathConstants.NODESET);
                            //elementToChange = (Element) difference.getTestNodeDetail().getNode();
                            elementToChange = (Element) testNodes.item(0);
                            elementToChange.setAttribute("state", "deleted");
                            System.out.println("YYY " + elementToChange.getAttribute("name") + " " + elementToChange.getAttribute("state"));
                        }
                    }
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(Differ.class.getName()).log(Level.SEVERE, null, ex); //some monster
                }
                
                //System.out.println(attr + ": " + oldValue + " -> " + newValue); //control print
            }
        }

        fileManager.createXMLFile(testDoc); // Write to XML - not necessary
        System.out.println("testDoc: " + testDoc.getDocumentURI());
        return FileInfoConverter.domToDiffInfo2(testDoc);
    }
}
