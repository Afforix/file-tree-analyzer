/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jindra
 */
public class Diff extends FileInfo {

    private ItemState state;
    private final String newName="";
    private final boolean newDirectory=false;
    private final boolean newSymbolicLink=false;
    private final Long newSize=null;
    private final Date newCreationTime=null;
    private final Date newLastAccessTime=null;
    private final Date newLastModifiedTime=null;
    
    public Diff(Path file, BasicFileAttributes attributes) {
        super(file, attributes);
        
    }
     public ItemState getState() {
        return state;
    }
    public String getNewName() {
        return newName;
    }

    public boolean isNewDirectory() {
        return newDirectory;
    }

    public boolean isNewSymbolicLink() {
        return newSymbolicLink;
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
}
