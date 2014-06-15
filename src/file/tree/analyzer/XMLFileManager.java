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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author martina
 */
public class XMLFileManager {

    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
    private Path analysesPath;

    /**
     *
     * @param path path of the directory with analyses
     */
    public XMLFileManager(String path) {

        analysesPath = Paths.get(path);
        File dir = analysesPath.toFile();

        if (!dir.exists()) {
            dir.mkdir();
        } else if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Argument path isn't directory.");
        }

        try {
            analysesPath = analysesPath.toRealPath();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets XML DOM and saves it.
     *
     * @param xmlDom XML DOM to be saved
     * @return name of created file
     */
    public String createXMLFile(Document xmlDom) {
        if (xmlDom == null) {
            throw new IllegalArgumentException("xmlDom is null");
        }

        try {

            File file = new File(analysesPath.toFile(), getTimestamp() + ".xml");
            Transformer t = TransformerFactory.newInstance().newTransformer();

            //set indent
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            t.transform(new javax.xml.transform.dom.DOMSource(xmlDom), new javax.xml.transform.stream.StreamResult(file));
            logger.log(Level.INFO, "File {0} created.", file.toPath().getFileName().toString());
            return file.toPath().getFileName().toString();
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "Cannot save file.", ex);
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
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
        return timeStampFormat.format(date);
    }

    /**
     * Deletes XML file of given name.
     *
     * @param fileName name of the XML file to be deleted
     */
    public void deleteXMLFile(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName is null");
        }

        File analysesDirectory = analysesPath.toFile();
        File xmlFile = null;

        //set filter for xml files
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
            logger.log(Level.SEVERE, "File not found!");
            return;
        }

        try {
            String name = xmlFile.toPath().getFileName().toString();
            xmlFile.delete();
            logger.log(Level.INFO, "File {0} deleted.", name);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot delete file.", ex);
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

        //set filter for xml files
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

        logger.log(Level.INFO, "{0} analyses found.", xmlFiles.length);

        return xmlFilesNames;
    }

    /**
     * Returns XML DOM loaded from XML file.
     *
     * @param fileName name of the xml file
     * @return XML DOM loaded from XML file
     */
    public Document findXMLFile(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName is null");
        }

        Document xmlDoc = this.findXMLFile(fileName, false);
        return xmlDoc;
    }

    /**
     * Returns XML DOM loaded from XML file.
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

            if (turnOffDefferedDom) {
                dbFactory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            }

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            if (xmlFile != null) {
                xmlDoc = dBuilder.parse(xmlFile);
            } else {
                logger.log(Level.SEVERE, "File does not exist.");
            }

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "open {0} ", fileName);
        return xmlDoc;
    }

    /**
     * Method for effective obtaining of attribute path from the given XML.
     * 
     * @param file file to obtain path from
     * @return attribute path
     */
    public String getPathFromXML(String file) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) throws SAXException {
                    String path = attributes.getValue("path");
                    //stop parsing by throwing exception
                    throw new SAXException(path);
                }
            };
                    
            saxParser.parse(new File(analysesPath.toFile(), file), handler);
        } catch (SAXException e) {
            return e.getMessage();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "unknown file{0}", file);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, "SAX configuration failed");
        }
        
        //error occured
        return null;
    }
}
