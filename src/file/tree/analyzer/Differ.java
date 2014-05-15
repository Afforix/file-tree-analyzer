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
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for finding differences between two XML docs, storing them and
 * converting to FileInfo Note: XMLUnit library
 * (http://sourceforge.net/projects/xmlunit/) is needed In this project should
 * be library stored in ./build/xmlunit-1.5.jar
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
        try {
            differ.diffXMLs("./saved_analyses", "2014-05-15T222222.xml", "2014-05-15T111111.xml");
        } catch (IOException e) {
            System.out.println("error in main");
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
     */
    public void diffXMLs(String cwd, String controlName, String testName) throws IOException {

        XMLFileManager fileManager = new XMLFileManager(cwd);

        controlDoc = fileManager.findXMLFile(controlName, true);
        olderDoc = fileManager.findXMLFile(testName, true);
        testDoc = (Document) olderDoc.cloneNode(true); // for modifying attrs, we don't want to edit source XML currentStateDoc    

        Diff diff = new Diff(controlDoc, testDoc);
        ElementNameAndTextQualifier elementNameandTextQualifier = new ElementNameAndTextQualifier();
        diff.overrideElementQualifier(elementNameandTextQualifier);
        DetailedDiff detailedDiff = new DetailedDiff(diff);

        List allDifferences = detailedDiff.getAllDifferences();
        ListIterator listIterator = allDifferences.listIterator();

        Difference difference; //one item from allDifferences List
        Element elementToChange;

        Boolean modified = false;
        String modifiedElement = null; //path to modified element
        while (listIterator.hasNext()) {
            difference = (Difference) listIterator.next();
            NodeDetail controlNodeDetail = difference.getControlNodeDetail();
            NodeDetail testNodeDetail = difference.getTestNodeDetail();
            
            String description = difference.getDescription();
            String whatChanged = description.substring(0, description.indexOf(" "));
            

            if (whatChanged.equals("attribute")) {
                // Get what has changed
                String xpathToAttr = controlNodeDetail.getXpathLocation();
                String xpathToElem = xpathToAttr.substring(0, xpathToAttr.indexOf("@") - 1); // /document[1]/@attr                
                String attr = xpathToAttr.substring(xpathToAttr.indexOf('@') + 1); // attr is after @ in xpath
                String newValue = testNodeDetail.getValue();
                String oldValue = controlNodeDetail.getValue();

                XPath testXPath = XPathFactory.newInstance().newXPath();
                XPath controlXPath = XPathFactory.newInstance().newXPath();
                try {
                    //TODO: find out how to cast Node to Element, then avoid next line
                    //and create elementToChange using testNodeDetail.getNode()
                    //like Element elementToChange = (Element) testNodeDetail.getNode();
                    NodeList testNodes = (NodeList) testXPath.evaluate(xpathToElem, testDoc.getDocumentElement(), XPathConstants.NODESET);
                    elementToChange = (Element) testNodes.item(0);

                    //TODO: possible node states: unmodified,modified,created,deleted
                   
                   /*
                   NodeList controlNodes = (NodeList) controlXPath.evaluate(xpathToElem, controlDoc.getDocumentElement(), XPathConstants.NODESET);
                   if(controlNodes.getLength() == 0){
                       elementToChange.setAttribute("itemState", "created");                       
                   }*/
                    
                    elementToChange.setAttribute("new-" + attr, newValue);
                    
                    if(!xpathToAttr.equals(modifiedElement))
                    {
                        modifiedElement = xpathToAttr;
                        elementToChange.setAttribute("itemState", "modified");
                        //System.out.println("ITEMSTATE CHANGED");
                    }
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(Differ.class.getName()).log(Level.SEVERE, null, ex); //some monster
                }
                
                //System.out.println(attr + ": " + oldValue + " -> " + newValue); //control print
            }
        }

        fileManager.createXMLFile(testDoc); // Write to XML - not necessary
    }
    
    
    //TODO: convert DOM to extended FileInfo
}
