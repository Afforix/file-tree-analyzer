package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;

public class Controller {

// <editor-fold defaultstate="collapsed" desc="FXML variables">
    @FXML
    private Parent root;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TreeView<FileInfo> treeView;
    @FXML
    private TableView<Pair<String, String>> tableView;
    @FXML
    private TableColumn<Pair<String, String>, String> attributeName;
    @FXML
    private TableColumn<Pair<String, String>, String> attribute;
    @FXML
    private ComboBox<String> openComboBox;
    @FXML
    private ComboBox<String> diffComboBox;

    // </editor-fold>
    

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        attributeName.setCellValueFactory(
                new PropertyValueFactory<>("key"));
        attribute.setCellValueFactory(
                new PropertyValueFactory<>("value"));       
        attribute.setCellFactory(TextFieldTableCell.forTableColumn());        
        tableView.setPlaceholder(new Label("Properties"));
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        

        treeView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                            Object newValue) {

                        if (newValue != null) {
                            TreeItem<FileInfo> selectedItem = (TreeItem<FileInfo>) newValue;
                            tableView.setItems(selectedItem.getValue().getPairedVariables());
                        } else {
                            tableView.setPlaceholder(new Label("Properties"));
                        }
                    }

                });

        XMLFileManager xmlFileManager = new XMLFileManager(("./saved_analyses"));
        List<String> xmlFiles = xmlFileManager.findAllXMLFiles();

        openComboBox.getItems().addAll(xmlFiles);
        diffComboBox.getItems().addAll(xmlFiles);
    }

    @FXML
    void handleOpenComboBoxAction(ActionEvent event) {
        ComboBox<String> source = (ComboBox<String>) event.getSource();
        System.out.println("Open " + source.getValue());
    }

    @FXML
    void handleDiffComboBoxAction(ActionEvent event) {
        ComboBox<String> source = (ComboBox<String>) event.getSource();
        System.out.println("Diff " + source.getValue());
    }

    @FXML
    void handleNewAnalysisAction(ActionEvent event) {

        root.setDisable(true);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser
                .showDialog(root.getScene().getWindow());

        if (selectedDirectory != null) {
            try {
                FileInfo directory = DiskExplorer
                        .getFileTree(selectedDirectory.getAbsolutePath());

                if (directory != null) {
                    TreeItem<FileInfo> treeRoot = new TreeItem<>(directory);
                    addRecursively(treeRoot);
                    treeView.setRoot(treeRoot);
                    treeRoot.setExpanded(true);
                }
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(Controller.class.getName()).
                    log(Level.SEVERE, "Analysis failed.");
        }
        
        root.setDisable(false);
    }

// <editor-fold defaultstate="collapsed" desc="Menu actions">
    @FXML
    void handleMenuDiffToAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("Diff " + selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void handleMenuDiffToCurrentAction(ActionEvent event) {
        System.out.println("Diff current state");
    }

    @FXML
    void handleOptionsAction(ActionEvent event) {

    }

    @FXML
    void handleQuitAction(ActionEvent event) {
        System.exit(0);
    }
    // </editor-fold>

    private void addRecursively(TreeItem<FileInfo> element) {
        for (FileInfo f : element.getValue().getChildren()) {
            TreeItem<FileInfo> item = new TreeItem<>(f);
            element.getChildren().add(item);
            if (f.isDirectory()) {
                addRecursively(item);
            }
        }
    }
}
