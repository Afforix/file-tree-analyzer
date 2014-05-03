/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author ansy
 */
public class FileTreeAnalyzer extends Application{

      
    @Override
    public void start(Stage primaryStage) throws IOException {     
   //    setUserAgentStylesheet(STYLESHEET_MODENA); //STYLESHEET_CASPIAN
        Parent root = FXMLLoader.load(getClass().getResource("FileTreeAnalyzer.fxml"));      
        Scene scene = new Scene(root);  
       
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Tree Analyzer");
        primaryStage.show();        
        
        //FOR TESTING ONLY
        System.out.println("Contents of the parent directory:");
        FileInfo directory = null;
        try {
            directory = DiskExplorer.getFileTree("..");
            directory.print();
            System.out.println("DONE");
        } catch (IOException e) {
            System.err.println("Analysis failed.");
            e.printStackTrace();
        }                 
        //END TESTING
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         launch(args);     
    }
}
