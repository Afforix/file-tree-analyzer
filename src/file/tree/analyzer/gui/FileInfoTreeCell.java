package file.tree.analyzer.gui;

import file.tree.analyzer.DiffInfo;
import file.tree.analyzer.FileInfo;
import file.tree.analyzer.utils.Utils;
import javafx.scene.control.TreeCell;
import javafx.scene.paint.Color;

/**
 *
 * @author ansy
 */
public final class FileInfoTreeCell extends TreeCell<FileInfo> {

    @Override
    protected void updateItem(final FileInfo info, boolean empty) {
        super.updateItem(info, empty);
        if (!empty && info != null) {
            setTextFill(Color.BLACK);
            
            if (info instanceof DiffInfo) {
                DiffInfo diff = (DiffInfo) info;

                switch (diff.getState()) {

                    case MODIFIED:
                        setTextFill(Color.BLUE);
                        break;

                    case CREATED:
                        setTextFill(Color.GREEN);
                        break;
                    case DELETED:
                        setTextFill(Color.RED);
                        break;
                }

            }
            setGraphic(Utils.getImageView(info));
            setText(info.toString());

        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
