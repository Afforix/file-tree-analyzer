/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer.utils;

import file.tree.analyzer.XMLFileManager;
import file.tree.analyzer.gui.ComboBoxItem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import org.w3c.dom.Document;

/**
 *
 * @author ansy
 */
public class Utils {
    
    public static List<ComboBoxItem> FilenameToComboBoxItem(List<String> list,XMLFileManager xmlFileManager){
        List<ComboBoxItem> out = new ArrayList<>();
        
        for(String s : list){
            out.add(FilenameToComboBoxItem(s,xmlFileManager));
        }     
        
        return out;
    }
    
    public static ComboBoxItem FilenameToComboBoxItem(String file,XMLFileManager xmlFileManager){
        Document doc = xmlFileManager.findXMLFile(file);
        String path = doc.getDocumentElement().getAttribute("path");
        
        return new ComboBoxItem(path, file);
    }
    
    public static Image FileToImg(File file) {
        
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

}
