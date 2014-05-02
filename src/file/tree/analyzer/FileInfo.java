/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package file.tree.analyzer;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author afforix
 */
public class FileInfo implements Comparable<FileInfo> {
    
    private final String name;
    private final boolean directory;
    private final boolean symbolicLink;
    private final Long size;
    private final Date creationTime;
    private final Date lastAccessTime;
    private final Date lastModifiedTime;
    private final List<FileInfo> children;

    public FileInfo(Path file, BasicFileAttributes attributes) {
        if (file == null) throw new NullPointerException("file");
        if (attributes == null) throw new NullPointerException("attributes");
        
        //needs special treatment with root ("/")
        //TEST ON WINDOWS!
        if (file.getFileName() == null) name = "/";
        else name = file.getFileName().toString();
        symbolicLink = attributes.isSymbolicLink();
        
        directory = attributes.isDirectory();
        if (directory) {
            children = new ArrayList<>();
            size = null;
        } else {
            children = null;
            size = attributes.size();            
        }
        
        creationTime = new Date(attributes.creationTime().toMillis());
        lastAccessTime = new Date(attributes.lastAccessTime().toMillis());
        lastModifiedTime = new Date(attributes.lastModifiedTime().toMillis());
    }
    
    public void addChild(FileInfo file) {
        if (file == null) throw new NullPointerException("file");
        if (directory) {
            children.add(file);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }

    @Override
    public String toString() {
        return "FileInfo{" + "name=" + name + ", directory=" + directory + ", symbolicLink=" + symbolicLink + ", size=" + size + ", creationTime=" + creationTime + ", lastAccessTime=" + lastAccessTime + ", lastModifiedTime=" + lastModifiedTime + '}';
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isSymbolicLink() {
        return symbolicLink;
    }

    public Long getSize() {
        return size;
    }

    public Date getCreationTime() {
        return new Date(creationTime.getTime());
    }

    public Date getLastAccessTime() {
        return new Date(lastAccessTime.getTime());
    }

    public Date getLastModifiedTime() {
        return new Date(lastModifiedTime.getTime());
    }

    public List<FileInfo> getChildren() {
        if (directory) {
            return Collections.unmodifiableList(children);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }
    
    public void print() {
        print(0);
    }
    
    private void print(int level) {
        System.out.println(name);
        if (!directory) return;
        
        for (FileInfo file: children) {
            for (int i = 0; i <= level; i++) {
                System.out.print(" ");
            }
            file.print(level + 1);
        }
    }

    @Override
    public int compareTo(FileInfo o) {
        return name.compareTo(o.getName());
    }
    
    public void sortChildren() {
        if (directory) {
            Collections.sort(children);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }
}
