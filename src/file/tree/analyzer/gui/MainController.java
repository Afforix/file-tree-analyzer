package file.tree.analyzer.gui;

import file.tree.analyzer.DiskExplorer;
import file.tree.analyzer.FileInfo;
import file.tree.analyzer.FileInfoConverter;
import file.tree.analyzer.XMLFileManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

public class MainController {

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
    @FXML
    private MenuItem menuDelete;
    @FXML
    private MenuItem menuClear;
    @FXML
    private MenuItem menuDiffTo;
    @FXML
    private MenuItem menuDiffToCurrent;
    @FXML
    private CheckBox fullDiffCheckBox;

    // </editor-fold>
    private final XMLFileManager xmlFileManager = new XMLFileManager(("./saved_analyses"));
    private boolean treeAlreadyLoaded = false;

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

        openComboBox.getItems().addAll(xmlFileManager.findAllXMLFiles());
        diffComboBox.getItems().addAll(xmlFileManager.findAllXMLFiles());
    }

    @FXML
    void handleOpenComboBoxAction(ActionEvent event) {

        if (!treeAlreadyLoaded) { // to prevent double loading when creating new analysis
            ComboBox<String> source = (ComboBox<String>) event.getSource();
            if (source.getValue() == null || source.getValue().isEmpty()) {
                clear();
                return;
            }
            FileInfo dir = FileInfoConverter.domToFileInfo(xmlFileManager.findXMLFile(source.getValue()));
            loadFile(dir);
            tableView.setItems(null);
            tableView.getSelectionModel().clearSelection();
        } else {
            treeAlreadyLoaded = false;
        }
        if (menuClear.isDisable()) {
            enableItems(true, false);
        }

    }

    @FXML
    void handleDiffComboBoxAction(ActionEvent event) {
        ComboBox<String> source = (ComboBox<String>) event.getSource();
        System.out.println("Diff " + source.getValue());
        clear();
    }

    @FXML
    void handleNewAnalysisAction(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser
                .showDialog(root.getScene().getWindow());
        root.setDisable(true);
        if (selectedDirectory != null) {
            try {
                FileInfo directory = DiskExplorer
                        .getFileTree(selectedDirectory.getAbsolutePath());
                if (directory != null) {
                    loadFile(directory);
                    treeAlreadyLoaded = true;
                    // save directory                   
                    String file = xmlFileManager.createXMLFile(FileInfoConverter.fileInfoToDom(directory));
                    openComboBox.getItems().add(file);
                    diffComboBox.getItems().add(file);
                    openComboBox.getSelectionModel().select(file);
                    enableItems(true, false);
                }
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(MainController.class.getName()).
                    log(Level.SEVERE, "Analysis failed.");
        }
        root.setDisable(false);

    }

// <editor-fold defaultstate="collapsed" desc="Menu actions">
    @FXML
    void handleDeleteAnalysisAction(ActionEvent event) {
        String value = openComboBox.getValue();
        if (value != null) {
            xmlFileManager.deleteXMLFile(value);
            openComboBox.getItems().remove(value);
            diffComboBox.getItems().remove(value);
            clear();
        }
    }

    @FXML
    void handleClearAction(ActionEvent event) {
        clear();
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
    void handleAboutAction(ActionEvent event) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(root.getScene().getWindow());
            Parent parent = FXMLLoader.load(getClass().getResource("AboutWindow.fxml"));
            stage.setScene(new Scene(parent));
            stage.setTitle("About");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void handleQuitAction(ActionEvent event) {
        System.exit(0);
    }
    // </editor-fold>

    private void loadFile(FileInfo directory) {
        TreeItem<FileInfo> treeRoot = new TreeItem<>(directory);
        addRecursively(treeRoot);
        treeView.setRoot(treeRoot);
        treeRoot.setExpanded(true);
    }

    private void addRecursively(TreeItem<FileInfo> element) {
        for (FileInfo f : element.getValue().getChildren()) {
            TreeItem<FileInfo> item = new TreeItem<>(f);
            try {
                ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(f.toFile());
                java.awt.Image image = icon.getImage();
                Image img = SwingFXUtils.toFXImage(toBufferedImage(image), null);
                item.setGraphic(new ImageView(img));
            } catch (Exception e) {
            }
            
            element.getChildren().add(item);
            if (f.isDirectory()) {
                addRecursively(item);
            }
        }
    }

    private void clear() {
        openComboBox.getSelectionModel().clearSelection();
        openComboBox.setPromptText("Choose File ...");
        diffComboBox.getSelectionModel().clearSelection();
        diffComboBox.setPromptText("Choose File ...");
        treeView.setRoot(null);
        tableView.setItems(null);
        tableView.getSelectionModel().clearSelection();
        enableItems(false, false);
    }

    private void enableItems(boolean items, boolean checkbox) {
        menuDelete.setDisable(!items);
        menuDiffTo.setDisable(!items);
        menuClear.setDisable(!items);
        diffComboBox.setDisable(!items);
        menuDiffToCurrent.setDisable(!items);
        fullDiffCheckBox.setDisable(!checkbox);
    }

    /**
     * Converts a given Image into a BufferedImage
     *
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
