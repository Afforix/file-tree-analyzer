/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 *
 * @author jindra
 */
public class DiffInfo extends FileInfo {

    private final ItemState state;
    private final List<DiffInfo> diffChildren;
    private final boolean newAccesibility;
    private final int newNumberOfFiles;
    private final int newNumberOfDirectories;
    private final Long newSize;
    private final Date newCreationTime;
    private final Date newLastAccessTime;
    private final Date newLastModifiedTime;
    
    public DiffInfo(Path file, BasicFileAttributes attributes) {
        super(file, attributes);
        this.state = null;
        this.diffChildren = null;
        this.newAccesibility = false;   
        this.newNumberOfFiles = 0; //or something else?
        this.newNumberOfDirectories = 0;
        this.newSize = null;
        this.newCreationTime = null;
        this.newLastAccessTime = null;
        this.newLastModifiedTime = null;
    }
    
    public DiffInfo(String name, String path, boolean directory, boolean symbolicLink, boolean accessibility, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories,
        ItemState state, List<DiffInfo> diffChildren, boolean newAccessibility, int newNumberOfFiles, int newNumberOfDirectories, Long newSize, Date newCreationTime, Date newLastAccessTime, Date newLastModifiedTime) {
        super(name, path, directory, symbolicLink, accessibility, size, creationTime, lastAccessTime, lastModifiedTime, children, numberOfFiles, numberofDirectories);
        this.state = state;
        this.diffChildren = diffChildren;
        this.newAccesibility = newAccessibility;
        this.newNumberOfFiles = newNumberOfFiles;
        this.newNumberOfDirectories = newNumberOfDirectories;
        this.newSize = newSize;
        this.newCreationTime = newCreationTime;
        this.newLastAccessTime = newLastAccessTime;
        this.newLastModifiedTime = newLastModifiedTime;
    }
    
    public ItemState getState() {
        return state;
    }
    
    public boolean isNewlyAccessible() { //or how should it be named
        return newAccesibility;
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
    
    public int getNewNumberOfFiles() {
        return newNumberOfFiles;
    }
    
    public int getNewNumberOfDirectories() {
        return newNumberOfDirectories;
    }

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
