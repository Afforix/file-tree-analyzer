/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import file.tree.analyzer.gui.RowInfo;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author jindra
 */
public class DiffInfo extends FileInfo {

    protected ItemState state;
    protected List<DiffInfo> diffChildren;
    protected boolean newSymbolicLink;
    protected boolean newAccessibility;
    protected int newNumberOfFiles;
    protected int newNumberOfDirectories;
    protected Long newSize;
    protected Date newCreationTime;
    protected Date newLastAccessTime;
    protected Date newLastModifiedTime;

    /**
     * @deprecated  
     */
    public DiffInfo(String name, String path, boolean directory, boolean symbolicLink, boolean accessibility, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories,
            ItemState state, List<DiffInfo> diffChildren, boolean newSymbolicLink, boolean newAccessibility, int newNumberOfFiles, int newNumberOfDirectories, Long newSize, Date newCreationTime, Date newLastAccessTime, Date newLastModifiedTime) {
        super(name, path, directory, symbolicLink, accessibility, size, creationTime, lastAccessTime, lastModifiedTime, children, numberOfFiles, numberofDirectories);
        this.state = state;
        this.newSymbolicLink = newSymbolicLink;
        this.diffChildren = diffChildren;
        this.newAccessibility = newAccessibility;
        this.newNumberOfFiles = newNumberOfFiles;
        this.newNumberOfDirectories = newNumberOfDirectories;
        this.newSize = newSize;
        this.newCreationTime = newCreationTime;
        this.newLastAccessTime = newLastAccessTime;
        this.newLastModifiedTime = newLastModifiedTime;
    }
    
    
    public DiffInfo() {
        super();
    }
    
    public DiffInfo(Path file) {
        super(file);
    }

    @Override
    public ObservableList<RowInfo> getTableRows() {

        ArrayList<RowInfo> list = new ArrayList<>();     

        list.add(new RowInfo("Name", name, "(diffed)" + name));   
    
        if (!directory) {
            addToList(list, "Size", size, getNewSize());
             } else {
            addToList(list, "Number of Files", numberOfFiles, getNewNumberOfFiles());
            addToList(list, "Number of Directories", numberOfDirectories, getNewNumberOfDirectories());            
        }
         addToList(list, "Creation Time", creationTime, getNewCreationTime());  
         addToList(list, "Last Access Time", lastAccessTime, getNewLastAccessTime());  
          addToList(list, "Symbolic Link", symbolicLink, newSymbolicLink); 
          addToList(list, "Path", path, null); 
          list.add(new RowInfo("Accessible", Boolean.toString(isAccessible()), Boolean.toString(!isNewlyAccessible())));
        
        return FXCollections.observableArrayList(list);
    }

    private void addToList(ArrayList<RowInfo> list, String key, Object value, Object newValue) {
        String val = "";
        String newVal = "";
        
        if (!isAccessible() && isNewlyAccessible()) {
            return;
        }
        if (isAccessible()) {
            val = objectToString(value);
        }
        if (!isNewlyAccessible()) {
            newVal = objectToString(newValue);
        }

        list.add(new RowInfo(key, val, newVal));
    }

    private String objectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof Long) {
            return humanReadableByteCount((Long) obj, true) + " (" + (Long) obj + " bytes)";
        }
        if (obj instanceof Integer) {
            return String.valueOf((int) obj);
        }
        if (obj instanceof Boolean) {
            return Boolean.toString((boolean) obj);
        }
        if (obj instanceof Date) {
            Date objDate = (Date) obj;
            return objDate.toString();
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return "";
    }
    
    public void addChild(DiffInfo child) {
        if (isDirectory()) {
            if (diffChildren == null) {
                diffChildren = new ArrayList<>();
            }
            diffChildren.add(child);
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }
    
    public List<DiffInfo> getDiffChildren() {
        if (!isAccessible() && !isNewlyAccessible()) {
            throw new IllegalStateException("file is not accessible: " + getPath());
        }

        if (isDirectory()) {
            if (diffChildren == null) {
                return Collections.emptyList();
            } else {
                return Collections.unmodifiableList(diffChildren);
            }
        } else {
            throw new IllegalStateException("Files don't have children! " + getPath());
        }
    }

    /**
     * @return the state
     */
    public ItemState getState() {
        return state;
    }

    /**
     * @return the newSymbolicLink
     */
    public boolean isNewlySymbolicLink() {
        return newSymbolicLink;
    }

    /**
     * @return the newAccesibility
     */
    public boolean isNewlyAccessible() {
        return newAccessibility;
    }

    /**
     * @return the newNumberOfFiles
     */
    public int getNewNumberOfFiles() {
        return newNumberOfFiles;
    }

    /**
     * @return the newNumberOfDirectories
     */
    public int getNewNumberOfDirectories() {
        return newNumberOfDirectories;
    }

    /**
     * @return the newSize
     */
    public Long getNewSize() {
        return newSize;
    }

    /**
     * @return the newCreationTime
     */
    public Date getNewCreationTime() {
        return newCreationTime;
    }

    /**
     * @return the newLastAccessTime
     */
    public Date getNewLastAccessTime() {
        return newLastAccessTime;
    }

    /**
     * @return the newLastModifiedTime
     */
    public Date getNewLastModifiedTime() {
        return newLastModifiedTime;
    }

    /**
     * @param state the state to set
     */
    public void setState(ItemState state) {
        this.state = state;
    }

    /**
     * @param newSymbolicLink the newSymbolicLink to set
     */
    public void setNewSymbolicLink(boolean newSymbolicLink) {
        this.newSymbolicLink = newSymbolicLink;
    }

    /**
     * @param newAccesibility the newAccesibility to set
     */
    public void setNewAccesibility(boolean newAccesibility) {
        this.newAccessibility = newAccesibility;
    }

    /**
     * @param newNumberOfFiles the newNumberOfFiles to set
     */
    public void setNewNumberOfFiles(int newNumberOfFiles) {
        this.newNumberOfFiles = newNumberOfFiles;
    }

    /**
     * @param newNumberOfDirectories the newNumberOfDirectories to set
     */
    public void setNewNumberOfDirectories(int newNumberOfDirectories) {
        this.newNumberOfDirectories = newNumberOfDirectories;
    }

    /**
     * @param newSize the newSize to set
     */
    public void setNewSize(Long newSize) {
        this.newSize = newSize;
    }

    /**
     * @param newCreationTime the newCreationTime to set
     */
    public void setNewCreationTime(Date newCreationTime) {
        this.newCreationTime = newCreationTime;
    }

    /**
     * @param newLastAccessTime the newLastAccessTime to set
     */
    public void setNewLastAccessTime(Date newLastAccessTime) {
        this.newLastAccessTime = newLastAccessTime;
    }

    /**
     * @param newLastModifiedTime the newLastModifiedTime to set
     */
    public void setNewLastModifiedTime(Date newLastModifiedTime) {
        this.newLastModifiedTime = newLastModifiedTime;
    }
}
