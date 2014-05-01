package file.tree.analyzer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class Controller {

    @FXML
    private Parent root;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TreeView<File> treeView;

    @FXML
    private TableView<TestClassAttribute> tableView;

    @FXML
    private TableColumn<TestClassAttribute, String> attributeName;

    @FXML
    private TableColumn<TestClassAttribute, String> attribute;

    @FXML
    private ComboBox<String> openComboBox;

    @FXML
    private ComboBox<String> diffComboBox;

    private ObservableList<TestClassAttribute> testData;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        attributeName.setCellValueFactory(
                new PropertyValueFactory<>("attributeName"));

        attribute.setCellValueFactory(
                new PropertyValueFactory<>("attribute"));

        testData = FXCollections.observableArrayList(
                new TestClassAttribute("Name", "test.txt"),
                new TestClassAttribute("Type", "plain text document (text/plain)"),
                new TestClassAttribute("Size", "100 bytes"),
                new TestClassAttribute("Location", "/home")
        );

        tableView.setItems(testData);

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

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(root.getScene().getWindow());

        if (selectedDirectory != null) {

            System.out.println(selectedDirectory.getAbsolutePath());

            listFilesDemo(selectedDirectory);
        }

    }

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

    private void listFilesDemo(File selectedDirectory) {

        TreeItem<File> r = new TreeItem<>(selectedDirectory);

        for (File n : selectedDirectory.listFiles()) {
            r.getChildren().add(new TreeItem<>(n));
        }

        r.setExpanded(true);
        treeView.setRoot(r);
    }

    public class TestClassAttribute {

        private final SimpleStringProperty attributeName;
        private final SimpleStringProperty attribute;

        public TestClassAttribute(String attributeName, String attribute) {

            this.attribute = new SimpleStringProperty(attribute);
            this.attributeName = new SimpleStringProperty(attributeName);
        }

        public String getAttributeName() {
            return attributeName.get();
        }

        public String getAttribute() {
            return attribute.get();
        }
    }

}
