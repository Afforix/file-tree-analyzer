/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static javafx.scene.input.KeyCode.T;


/**
 *
 * @author jindra
 */
public class DiffInfo extends FileInfo {

    private ItemState state;
    //commented because newDirectory says the same as FileInfo.directory + state="created"
    //private final boolean newDirectory; 
    //same as newDirectory
    //private final boolean newSymbolicLink;
    private final Long newSize;
    private final Date newCreationTime;
    private final Date newLastAccessTime;
    private final Date newLastModifiedTime;
    
    public DiffInfo(Path file, BasicFileAttributes attributes) {
        super(file, attributes);
        //this.newDirectory = false;
        //this.newSymbolicLink = false;
        this.newSize = null;
        this.newCreationTime = null;
        this.newLastAccessTime = null;
        this.newLastModifiedTime = null;
    }
    
    public DiffInfo(String name, String path, boolean directory, boolean symbolicLink, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories,
        ItemState state, /*boolean newDirectory, boolean newSymbolicLink,*/ Long newSize, Date newCreationTime, Date newLastAccessTime, Date newLastModifiedTime) {
        super(name, path, directory, symbolicLink, size, creationTime, lastAccessTime, lastModifiedTime, children, numberOfFiles, numberofDirectories);
        this.state = state;
        //this.newDirectory = newDirectory;
        //this.newSymbolicLink = newSymbolicLink;
        this.newSize = newSize;
        this.newCreationTime = newCreationTime;
        this.newLastAccessTime = newLastAccessTime;
        this.newLastModifiedTime = newLastModifiedTime;
    }
    
    
    public ItemState getState() {
        return state;
    }
    
    /* boolean isNewDirectory() {
        return newDirectory;
    }

    public boolean isNewSymbolicLink() {
        return newSymbolicLink;
    }*/

    public Long getNewSize() {
        return newSize;
    }

    public Date getNewCreationTime() {
        return new Date(newCreationTime.getTime());
    }

    public Date getNewLastAccessTime() {
        return new Date(newLastAccessTime.getTime());
    }

    public Date getNewLastModifiedTime() {
        return new Date(newLastModifiedTime.getTime());
    }
    
    /*
    public List<DiffInfo> getChildrenInDiff() {
        if (this.isDirectory()) {
            return (List<DiffInfo>)children);
            //return (List<DiffInfo>) Collections.unmodifiableList((List<? extends T>) children);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }
    */
        /*
        if (this.isDirectory()) {
            List<FileInfo> fileInfo = this.getChildren();
            List<DiffInfo> diffInfo;
            for(Item i: diffInfo)
            //diffInfo.addAll(fileInfo);
            fileInfo.addAll(diffInfo);
            return Collections.unmodifiableList();
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
                */
    
    
}
