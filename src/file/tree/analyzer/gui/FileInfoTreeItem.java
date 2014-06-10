/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer.gui;

import file.tree.analyzer.DiffInfo;
import file.tree.analyzer.FileInfo;
import file.tree.analyzer.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author ansy
 */
class FileInfoTreeItem extends TreeItem<FileInfo> {

    private boolean hasLoadedChildren = false;

    public FileInfoTreeItem(FileInfo directory) {
        super(directory);
    }

    @Override
    public ObservableList<TreeItem<FileInfo>> getChildren() {
        if (hasLoadedChildren == false) {
            loadChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (hasLoadedChildren == false) {
            loadChildren();
        }
        return super.getChildren().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void loadChildren() {
        hasLoadedChildren = true;
        FileInfo elementVal = super.getValue();       
       
        if (elementVal.isDirectory() && elementVal.isAccessible()) {
            if (elementVal instanceof DiffInfo) {
                DiffInfo info = (DiffInfo)elementVal;
               for (DiffInfo f : info.getDiffChildren()) {
                    FileInfoTreeItem item = new FileInfoTreeItem(f);
                    super.getChildren().add(item);
                }      
            } else {
                for (FileInfo f : elementVal.getChildren()) {
                    FileInfoTreeItem item = new FileInfoTreeItem(f);
                    super.getChildren().add(item);
                }                
            }

        }
    }

}
