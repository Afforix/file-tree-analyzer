package file.tree.analyzer.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author ansy
 */

public class DialogController {

    @FXML
    private Button abortButton;

    @FXML
    private Label messageLabel;

    @FXML
    void handleAbortDialogWindowAction(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        ThreadSingleton.getInstance().getDiskExplorerThread().interrupt();
        stage.close();
    }
}
