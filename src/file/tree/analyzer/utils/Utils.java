/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer.utils;

import file.tree.analyzer.FileInfo;
import file.tree.analyzer.XMLFileManager;
import file.tree.analyzer.gui.ComboBoxItem;
import file.tree.analyzer.gui.MainController;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import org.w3c.dom.Document;

/**
 *
 * @author ansy
 */
public class Utils {
    
   
    private static Image genericFileImg;
    private static Image genericDirectoryImg;

    static {
        try {
            File genericFile = Files.createTempFile(null, "").toFile();
            File genericDirectory = Files.createTempDirectory(null).toFile();
            genericFileImg = Utils.FileToImg(genericFile);
            genericDirectoryImg = Utils.FileToImg(genericDirectory);
            genericFile.delete();
            genericDirectory.delete();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    public static ImageView getImageView(FileInfo info){
         Image img = null;
        try { // try to set icon           

            if (info.isDirectory()) {
                img = genericDirectoryImg;
            } else if (info.toFile().exists() && !info.isSymbolicLink()) {
                img = Utils.FileToImg(info.toFile());
            }            

        } catch (Exception e) {
        }

        if (img == null) {
            img = genericFileImg;            
        }
        return new ImageView(img);
    }
    
    
    
    public static List<ComboBoxItem> FilenameToComboBoxItem(List<String> list,XMLFileManager xmlFileManager){
        List<ComboBoxItem> out = new ArrayList<>();
        
        for(String s : list){
            out.add(FilenameToComboBoxItem(s,xmlFileManager));
        }     
        
        return out;
    }
    
    public static ComboBoxItem FilenameToComboBoxItem(String file,XMLFileManager xmlFileManager){
//        Document doc = xmlFileManager.findXMLFile(file);
//        String path = doc.getDocumentElement().getAttribute("path");
        String path = xmlFileManager.getPathFromXML(file);
        return new ComboBoxItem(path, file);
    }
    
    private static Image FileToImg(File file) {
        
        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView()
                .getSystemIcon(file);
        if (icon != null) {
            java.awt.Image image = icon.getImage();
            return SwingFXUtils.toFXImage(Utils.toBufferedImage(image), null);
            
        }
        return null;
    }
    
    /**
     * Converts a given Image into a BufferedImage
     * http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    private static BufferedImage toBufferedImage(java.awt.Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    /**
     *
     * @param bytes the value of bytes
     * @param si the value of si
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
