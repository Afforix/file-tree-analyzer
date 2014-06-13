/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ansy
 */
public class FileTreeAnalyzer extends Application {

    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
    private static FileHandler fh = null;

    @Override
    public void start(Stage primaryStage) throws IOException {
        initLogger();        
        try{
        Parent root = FXMLLoader.load(getClass().getResource("gui/MainWindow.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("File Tree Analyzer");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.show();
        }catch(Exception ex){
            logger.log(Level.SEVERE,ex.getMessage());
            throw ex;
        }

        //FOR TESTING ONLY
//        System.out.println("Contents of the parent directory:");
//        FileInfo directory = null;
//        try {
//            directory = DiskExplorer.getFileTree("..");
//            directory.print();
//            System.out.println("DONE");
//        } catch (IOException e) {
//            System.err.println("Analysis failed.");
//            e.printStackTrace();
//        }                 
        //END TESTING
    }

    private static void initLogger() {
        try {
            fh = new FileHandler("log.log",10240,1, true);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
       // Logger l = Logger.getLogger("");
        
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
