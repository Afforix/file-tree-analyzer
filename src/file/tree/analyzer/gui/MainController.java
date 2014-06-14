package file.tree.analyzer.gui;

import file.tree.analyzer.DiffInfo;
import file.tree.analyzer.Differ;
import file.tree.analyzer.FileInfo;
import file.tree.analyzer.FileInfoConverter;
import file.tree.analyzer.FileTreeAnalyzer;
import file.tree.analyzer.XMLFileManager;
import file.tree.analyzer.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;

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
    private TableView<RowInfo> tableView;
    @FXML
    private TableColumn<RowInfo, String> attributeName;
    @FXML
    private TableColumn<RowInfo, String> attribute;
    @FXML
    private TableColumn<RowInfo, String> changedAttribute;
    @FXML
    private ComboBox<ComboBoxItem> openComboBox;
    @FXML
    private ComboBox<ComboBoxItem> diffComboBox;
    @FXML
    private MenuItem menuDelete;
    @FXML
    private MenuItem menuClear;
    @FXML
    private MenuItem menuDiffToCurrent;
    @FXML
    private CheckBox fullDiffCheckBox;

    // </editor-fold>
    private final static Logger logger = Logger.getLogger(FileTreeAnalyzer.class.getName());
    private final XMLFileManager xmlFileManager = new XMLFileManager(("./saved_analyses"));
    private boolean treeAlreadyLoaded = false;
    private FileInfoTreeItem treeRoot;
    private FileInfoTreeItem treeRootFiltered;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        attributeName.setCellValueFactory(
                new PropertyValueFactory<>("key"));
        attribute.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        changedAttribute.setCellValueFactory(
                new PropertyValueFactory<>("newValue"));
        attribute.setCellFactory(TextFieldTableCell.forTableColumn());
        tableView.setPlaceholder(new Label("Properties"));

        changedAttribute.setCellFactory(new Callback<TableColumn<RowInfo, String>, TableCell<RowInfo, String>>() { //(TextFieldTableCell.forTableColumn());
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<RowInfo, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!isEmpty()) {
                            RowInfo info = (RowInfo) getTableRow().getItem();
                            this.setTextFill(info.getColor());
                            setText(item);
                        } else {
                            setText(null);
                        }
                    }

                };
            }
        }
        );

        tableView.getSelectionModel().setCellSelectionEnabled(true);

        treeView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue,
                            Object newValue) {

                        if (newValue != null) {
                            TreeItem<FileInfo> selectedItem = (TreeItem<FileInfo>) newValue;
                            tableView.setItems(selectedItem.getValue().getTableRows());
                        } else {
                            tableView.setPlaceholder(new Label("Properties"));
                        }
                    }
                });

        treeView.setCellFactory(new Callback<TreeView<FileInfo>, TreeCell<FileInfo>>() {
            @Override
            public TreeCell<FileInfo> call(TreeView<FileInfo> treeView) {
                return new FileInfoTreeCell();
            }
        });

        openComboBox.getItems().addAll(Utils.FilenameToComboBoxItem(
                xmlFileManager.findAllXMLFiles(), xmlFileManager));
        openComboBox.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<ComboBoxItem>() {
                    @Override
                    public void changed(ObservableValue<? extends ComboBoxItem> observable,
                            ComboBoxItem oldValue, ComboBoxItem newValue) {
                        if (newValue != null) {
                            filterItems(Paths.get(newValue.getPath()), diffComboBox, openComboBox.getItems());
                            diffComboBox.getSelectionModel().clearSelection();
                            diffComboBox.setPromptText("Choose File ...");
                            diffComboBox.setDisable(false);
                            menuDiffToCurrent.setDisable(false);
                            menuDelete.setDisable(false);
                            menuClear.setDisable(false);
                            fullDiffCheckBox.setDisable(true);
                        } else {
                            openComboBox.setPromptText("Choose File ...");
                            diffComboBox.setDisable(true);
                            menuDiffToCurrent.setDisable(true);
                            fullDiffCheckBox.setDisable(true);
                            menuDelete.setDisable(true);
                            menuClear.setDisable(true);
                            loadFile(null, false);
                        }

                    }
                });

    }

    @FXML
    void handleOpenComboBoxAction(ActionEvent event) {
        if (!treeAlreadyLoaded) { // to prevent double loading when creating new analysis
            ComboBox<ComboBoxItem> source = (ComboBox<ComboBoxItem>) event.getSource();
            if (source.getValue() == null) {
                return;
            }
            String fileName = source.getValue().getFile();
            FileInfo dir = FileInfoConverter.domToFileInfo(xmlFileManager.findXMLFile(fileName));
            loadFile(dir, false);
        } else {
            treeAlreadyLoaded = false;
        }

    }

    @FXML
    void handleDiffComboBoxAction(ActionEvent event) throws IOException {
        ComboBox<ComboBoxItem> source = (ComboBox<ComboBoxItem>) event.getSource();
        if (source.getValue() != null && openComboBox.getValue() != null) {
            diff(source.getValue().getFile(), openComboBox.getValue().getFile());
        }

    }

    @FXML
    void handleNewAnalysisAction(ActionEvent event) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser
                .showDialog(root.getScene().getWindow());

        newAnalysis(selectedDirectory, null);

    }

    @FXML
    void handleFullDiffAction(ActionEvent event) {
        CheckBox diff = (CheckBox) event.getSource();
        if (diff.isSelected()) {
            treeView.setRoot(treeRootFiltered);
        } else {
            treeView.setRoot(treeRoot);
        }     
    }

    private Stage InitializeWindow(String resource, String title) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        Parent parent = FXMLLoader.load(getClass().getResource(resource));
        stage.setScene(new Scene(parent));
        stage.setTitle(title);
        return stage;
    }

// <editor-fold defaultstate="collapsed" desc="Menu actions">
    @FXML
    void handleDeleteAnalysisAction(ActionEvent event) {
        ComboBoxItem value = openComboBox.getValue();
        if (value != null) {
            xmlFileManager.deleteXMLFile(value.getFile());
            openComboBox.getItems().remove(value);
            openComboBox.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void handleClearAction(ActionEvent event) {
        openComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    void handleMenuDiffToCurrentAction(ActionEvent event) throws IOException {

        if (openComboBox.getValue() != null) {
            newAnalysis(new File(openComboBox.getValue().getPath()), openComboBox.getValue());
        }
    }

    @FXML
    void handleOptionsAction(ActionEvent event) {

    }

    @FXML
    void handleAboutAction(ActionEvent event) {
        try {
            Stage stage = InitializeWindow("AboutWindow.fxml", "About");
            stage.show();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void handleQuitAction(ActionEvent event) {
        Platform.exit();
        logger.log(Level.INFO, "exit");
        System.exit(0);
    }
    // </editor-fold>

    private Stage newAnalysis(File selectedDirectory, ComboBoxItem oldItem) throws IOException {
        if (selectedDirectory != null) {
            Stage subStage = InitializeWindow("DialogWindow.fxml", "Status");
            subStage.setResizable(false);
            subStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    event.consume();
                }
            });
            subStage.show();
            Task<FileInfo> task = new DiskExplorerTask(selectedDirectory);

            task.stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> source,
                        Worker.State oldState, Worker.State newState) {

                    if (newState.equals(Worker.State.SUCCEEDED)) {
                        FileInfo dir = task.getValue();
                        if (dir != null) {
                            loadFile(dir, false);
                            treeAlreadyLoaded = true;
                            // save directory                   
                            String file = xmlFileManager
                                    .createXMLFile(FileInfoConverter.fileInfoToDom(dir));
                            ComboBoxItem displayString = Utils
                                    .FilenameToComboBoxItem(file, xmlFileManager);
                            openComboBox.getItems().add(displayString);
                            openComboBox.getSelectionModel().select(displayString);

                            if (oldItem != null) {
                                try {
                                    treeAlreadyLoaded = true;
                                    openComboBox.getSelectionModel().select(oldItem);
                                    String oldFile = oldItem.getFile();
                                    diff(file, oldFile);
                                    xmlFileManager.deleteXMLFile(file);
                                    openComboBox.getItems().remove(displayString);
                                    diffComboBox.setDisable(true);
                                } catch (IOException ex) {
                                    logger.log(Level.SEVERE, null, ex);
                                }
                            }

                            subStage.close();

                        }
                    } else if (newState.equals(Worker.State.FAILED)) {
                        logger.log(Level.SEVERE, "Analysis failed");
                    } else if (newState.equals(Worker.State.CANCELLED)) {
                        logger.log(Level.SEVERE, "Analysis was canceled");
                    }
                }
            });

            Thread thread = new Thread(task);
            ThreadSingleton.getInstance().setDiskExplorerThread(thread);
            thread.start();
            return subStage;

        } else {
            logger.log(Level.SEVERE, "No selected directory!");
        }
        return null;
    }

    private void diff(String first, String second) throws IOException {

        System.out.println("Diff " + first + " " + second); // log
        fullDiffCheckBox.setDisable(false);
        DiffInfo info = Differ.diffXMLs("./saved_analyses", first, second);
        loadFile(info, true);
        menuDelete.setDisable(true);
    }

    private void loadFile(FileInfo directory, boolean diff) {
        if (directory != null) {
            treeRoot = new FileInfoTreeItem(directory);
            treeRootFiltered = new FileInfoTreeItem(directory, true);
            
            treeRootFiltered.setExpanded(true);
            treeRoot.setExpanded(true);
            
           treeView.setRoot(treeRoot);
        } else {
            treeView.setRoot(null);
        }
        fullDiffCheckBox.setSelected(false);
        tableView.setItems(null);
        tableView.getSelectionModel().clearSelection();
    }

    private <T> void filterItems(Path filter, ComboBox<T> comboBox,
            List<T> items) {
        ComboBoxItem selectOpen = openComboBox.getSelectionModel().getSelectedItem();
        List<T> filteredItems = new ArrayList<>();

        if (selectOpen != null) {
            filter = filter.getName(filter.getNameCount() - 1);
            for (T item : items) {
                Path path = Paths.get(((ComboBoxItem) item).getPath());
                if (path.endsWith(filter) && !selectOpen.equals(item)) {
                    filteredItems.add(item);
                } else {
                    filteredItems.add(item);
                }
            }
        }
        comboBox.setItems(FXCollections.observableArrayList(filteredItems));
    }
}
