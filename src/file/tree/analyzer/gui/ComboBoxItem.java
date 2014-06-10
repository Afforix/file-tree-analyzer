/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer.gui;

/**
 *
 * @author ansy
 */
public class ComboBoxItem {

    private String path;
    private String file;

    public ComboBoxItem(String path, String file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path");
        }

        this.path = path;
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public String getFile() {
        return file;
    }

    @Override
    public String toString() {
        return path + " " + file;
    }

    @Override
    public int hashCode() {
        return path.hashCode()*31 + file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean flag = false;
         if (!(obj instanceof ComboBoxItem))
            return false;
         
        ComboBoxItem item = (ComboBoxItem) obj;
        if (path.equals(item.path) && file.equals(item.file) ) {
            flag = true;
        }
        return flag;
    }

}
