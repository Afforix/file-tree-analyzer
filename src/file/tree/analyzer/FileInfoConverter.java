/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

            // root element
            if (root.isDirectory()) {
                Element rootElement = doc.createElement("directory");
                rootElement.setAttribute("name", root.getName());
                doc.appendChild(rootElement);
                List<FileInfo> children = new ArrayList<>(root.getChildren());
                for (FileInfo child : children) {
                    childrenToDom(doc, rootElement, child);
                }
            } else {
                Element rootElement = doc.createElement("file");
                rootElement.setTextContent(root.getName());
                if(root.getSize() != null)
                    rootElement.setAttribute("size", root.getSize().toString() + " B");
                doc.appendChild(rootElement);
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileInfoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private static void childrenToDom(Document doc, Element parent, FileInfo root) {
        if (root.isDirectory()) {
            Element currentElement = doc.createElement("directory");
            currentElement.setAttribute("name", root.getName());
            //TODO size of the dirctory?
            parent.appendChild(currentElement);
            List<FileInfo> children = new ArrayList<>(root.getChildren());
            for (FileInfo child : children) {
                childrenToDom(doc, currentElement, child);
            }
        } else {
            Element currentElement = doc.createElement("file");
            currentElement.setTextContent(root.getName());
            if(root.getSize() != null)
                currentElement.setAttribute("size", root.getSize().toString());
            parent.appendChild(currentElement);
        }
    }
}
