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
import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.NodeTest;
import static org.junit.Assert.assertTrue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jindra
 */
public class myDiff {

    private Document testDoc;
    private Document controlDoc;

    //private final XMLFileManager xmlFileManager = new XMLFileManager(("./saved_analyses"));
    /**
     *
     * @param testPath name of the newer XML file
     * @param controlPath name of the the older XML file
     * @throws java.io.IOException
     *
     */
    public void DiffTwoXML(String controlPath, String testPath) throws IOException {
        XMLFileManager testManager = new XMLFileManager(testPath);
        XMLFileManager controlManager = new XMLFileManager(controlPath);

        testPath = "2014-05-10T041014.xml";
        controlPath = "2014-05-10T041013.xml";

        testDoc = testManager.findXMLFile(testPath);
        controlDoc = controlManager.findXMLFile(controlPath);

        //FileInfo testFileInfo = getFileTree(testPath);
        //FileInfo controlFileInfo = getFileTree(controlPath);
        Element rootEl = testDoc.getDocumentElement();
        NodeList nl = rootEl.getChildNodes();
        nl.
        System.out.println("x: " + nl.item(1).getNodeName() + " ");
        nl.item(1).setTextContent("ABCDEFGHI");
        nl = nl.item(1).getChildNodes();
        nl = rootEl.getChildNodes(); 
        System.out.println("x: " + nl.item(1).getNodeName() + " " + nl.item(1).getTextContent());
       
        CollectChildren(rootEl);
        /*
        for(int i = 0; i < nl.getLength(); i++)
        {
            
        }
        */
        //System.out.println("x: " + nl.item(0) + " ");

       
        /*
         while (true) {
         if (n == null) {
         break;
         }
         System.out.println("BBB: " + n.toString());
         n = n.getNextSibling();
         }
         */

    }

    private NodeList CollectChildren(Node n) {
        Element rootElement = (Element) n;
        NodeList children = n.getChildNodes();
        Node current;
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
               System.out.println(children.item(i));
               NamedNodeMap nm = current.getAttributes();
                for (int j = 0; j < nm.getLength(); j++) {
                System.out.print(nm.item(j) + "|");
            }
            }
            
        }
        return children;
    }

    

    /*
    1) vzit dva dom dokumenty
    2) vzit pomoci getChildren() jedno patro uzlu.
    3) vytridit uzly tak, abych porovnaval jen file x directory x symbolic link
    4) iterovat pres patro testDocumentu po jednom
    A) zjistit, jestli je tam nejaky diff (myDetailedDiff)
    B) pokud ano, tak iterovat po jednom v patre controlDocumentu a kdyz nastane match, tak zkopirovat control info do test infa.
       Pokud ne, tak nastavit priznak na nezmeneno / nove pridano
    5) bod 4)
    */

    private void testAllDifferences() throws Exception {
        System.out.println("testAllDifferences in progress");
        //String myControlXML = "<news><item id=\"1\">War</item>"
        //    + "<item id=\"2\">Plague</item><item id=\"3\">Famine</item></news>";
        //String myTestXML = "<news><item id=\"1\">Peace</item>"
        //    + "<item id=\"2\">Health</item><item id=\"3\">Plenty</item></news>";
        ComparisonController comparisonController = new ComparisonController() {};
        DifferenceEngine differenceEngine = new DifferenceEngine(comparisonController);
        Diff myDiff = new Diff(testDoc, controlDoc, );
        //DifferenceListnener dl = new DifferenceListener();
        DetailedDiff myDetailedDiff = new DetailedDiff(myDiff);
        
        //ID is name of file/directory
        myDetailedDiff.overrideElementQualifier(new ElementNameAndTextQualifier());
        
        List allDifferences = myDetailedDiff.getAllDifferences();
        ListIterator it = allDifferences.listIterator();
            
        //System.out.println(it.next());
        int i = 0;
        while (it.hasNext()) {
            System.out.println(i + " " + it.next());
            i++;
        }
    }

    public void diffNode() {

    }
/*
    public void testCompareToSkeletonXML() throws Exception {
        String myControlXML = "<locatio><street-address>22 any street</street-address><postcode>XY00 99Z</postcode></locatio>";
        String myTestXML = "<location><street-address>20 east cheap</street-address><postcode>EC3M 1EB</postcode></location>";
        DifferenceListener myDifferenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        org.custommonkey.xmlunit.Diff myDiff = new org.custommonkey.xmlunit.Diff(myControlXML, myTestXML);
        myDiff.overrideDifferenceListener(myDifferenceListener);
        assertTrue("test XML matches control skeleton XML " + myDiff, myDiff.similar());
    }
*/
    public static void main(String[] args) {
        myDiff md = new myDiff();
        try {
            md.DiffTwoXML(".\\\\saved_analyses", ".\\\\saved_analyses");
            md.testAllDifferences();
            //md.testCompareToSkeletonXML();

        } catch (Exception e) {
            System.out.println("error in main");
        }
    }
}
