/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer.gui;

 import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
/**
 *
 * @author ansy
 */
public class AboutController {
    
    @FXML
    void handleCloseAboutWindowAction(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
    Stage stage  = (Stage) source.getScene().getWindow();
    stage.close();
    }
}


