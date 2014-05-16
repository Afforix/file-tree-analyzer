/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.xml.sax.SAXException;

/**
 *
 * @author martina
 */
public class XMLFileManager {
    //TODO logging

    private  Path analysesPath;

    public XMLFileManager(String path) {
        
        analysesPath = Paths.get(path);        
        File dir = analysesPath.toFile();

        if (!dir.exists()) {
            dir.mkdir();
        }else if(!dir.isDirectory()){
            throw new IllegalArgumentException("Argument path isn't directory.");
        }
        
        try {
           analysesPath = analysesPath.toRealPath();
        } catch (IOException ex) {
            Logger.getLogger(XMLFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    /**
     * Gets XML DOM and saves it.
     *
     * @param xmlDom XML DOM to be saved
     * @return name of created file
     */
    public String createXMLFile(Document xmlDom) {
        try {

            File file = new File(analysesPath.toFile(),getTimestamp() + ".xml");
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().
                    transform(new javax.xml.transform.dom.DOMSource(xmlDom), new javax.xml.transform.stream.StreamResult(file));
            return file.toPath().getFileName().toString();
        } catch (TransformerException ex) {
            Logger.getLogger(XMLFileManager.class.getName()).log(Level.SEVERE, "Cannot save file.", ex);
            return null;
        }
    }

    /**
     * Returns string that represents current date timestamp.
     *
     * @return string that represents timestamp
     */
    private String getTimestamp() {
        Date date = new Date();
        //TODO timestamp format
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd'T'hhmmss");
        return timeStampFormat.format(date);
    }

    /**
     * Deletes XML file of given name.
     *
     * @param fileName name of the XML file to be deleted
     */
    public void deleteXMLFile(String fileName) {
        File analysesDirectory = analysesPath.toFile();
        File xmlFile = null;

        FilenameFilter xmlFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String fileName = name.toLowerCase();
                return fileName.endsWith(".xml");
            }
        };

        File[] xmlFiles = analysesDirectory.listFiles(xmlFilter);

        if (xmlFiles != null) {
            for (File f : xmlFiles) {
                if (f.getName().equals(fileName)) {
                    xmlFile = f;
                }
            }
        }

        if (xmlFile == null) {
            Logger.getLogger(XMLFileManager.class.getName()).log(Level.SEVERE, "File not found!");
            return;
        }

        try {
            xmlFile.delete();
        } catch (Exception ex) {
            Logger.getLogger(XMLFileManager.class.getName()).log(Level.SEVERE, "Cannot delete file.", ex);
        }

    }

    /**
     * Returns list of all names of XML files found in folder for saving
     * analyses.
     *
     * @return list of names of XML files
     */
    public List<String> findAllXMLFiles() {
        List<String> xmlFilesNames = new ArrayList<>();

        File analysesDirectory = analysesPath.toFile();

        FilenameFilter xmlFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String fileName = name.toLowerCase();
                return fileName.endsWith(".xml");
            }
        };

        File[] xmlFiles = analysesDirectory.listFiles(xmlFilter);
        if (xmlFiles != null) {
            for (File f : xmlFiles) {
                xmlFilesNames.add(f.getName());
            }
        }

        return xmlFilesNames;
    }

    /**
     * Returns XML DOM loaded from XML file.
     *
     * @param fileName name of the xml file
     * @return XML DOM loaded from XML file
     */
    public Document findXMLFile(String fileName) {        
        Document xmlDoc = this.findXMLFile(fileName, false);
        return xmlDoc;
    }
    
    /**
     * Returns XML DOM loaded from XML file.
     * added by Jindra because deferred DOM was causing troubles in Differ 
     * 
     * @param fileName name of the xml file
     * @param turnOffDefferedDom true to disable deffered version of DOM 
     * @return XML DOM loaded from XML file
     */
     public Document findXMLFile(String fileName, Boolean turnOffDefferedDom) {
        Document xmlDoc = null;

        try {
            File analysesDirectory = analysesPath.toFile();
            File xmlFile = null;

            FilenameFilter xmlFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String fileName = name.toLowerCase();
                    return fileName.endsWith(".xml");
                }
            };

            File[] xmlFiles = analysesDirectory.listFiles(xmlFilter);

            if (xmlFiles != null) {
                for (File f : xmlFiles) {
                    if (f.getName().equals(fileName)) {
                        xmlFile = f;
                    }
                }
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            
            if(turnOffDefferedDom)
            {
                dbFactory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            }
                        
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(xmlFile);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(XMLFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return xmlDoc;
    }
}
