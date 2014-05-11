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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 * Entity class for storing information about file or directory. In case of
 * directory, all children are accessible using instance of this class.
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
    private final String path;
    private int numberOfFiles;
    private int numberofDirectories;

    /**
     * Constructor designed to use during hard disk analysis.
     *
     * @throws NullPointerException if parameter file or attributes is null
     * @throws IllegalStateException if directory features are used on an object
     * representing file. Client must always check if the object is directory
     * using {@code isDirectory()} method when using methods for directories:
     * {@code getChildren()} and {@code addChild()}.
     * @param file file, which attributes are going to be stored
     * @param attributes attributes of the files
     */
    public FileInfo(Path file, BasicFileAttributes attributes) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }

        //needs special treatment with root, because root does not have  
        //a regular file name, so it's necessary to obtain root name
        if (file.getFileName() == null) {
            name = file.getRoot().toString();
        } else {
            name = file.getFileName().toString();
        }

        path = file.toAbsolutePath().toString();
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
        numberOfFiles = 0;
        numberofDirectories = 0;
    }

    /**
     *
     * @param name name of file or directory
     * @param directory is directory
     * @param symbolicLink is symbolic link
     * @param size size of file
     * @param creationTime creation time
     * @param lastAccessTime last access time
     * @param lastModifiedTime last modification time
     * @param children list of children
     * @param numberOfFiles number of files
     * @param numberofDirectories number of directories
     */
    @Deprecated
    public FileInfo(String name, boolean directory, boolean symbolicLink, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories) {
        this.name = name;
        this.directory = directory;
        this.symbolicLink = symbolicLink;
        this.size = size;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.children = children;
        this.numberOfFiles = numberOfFiles;
        this.numberofDirectories = numberofDirectories;
        this.path = ""; //TODO
    }

    /**
     * Constructor designed for conversion from XML DOM.
     *
     * @param name name of file
     * @param directory is directory
     * @param symbolicLink is symbolic link
     * @param size size
     * @param creationTime creation time
     * @param lastAccessTime last access time
     * @param lastModifiedTime last modification time
     * @param path full path
     */
    public FileInfo(String name, boolean directory, boolean symbolicLink, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, String path) {
        this.name = name;
        this.directory = directory;
        this.symbolicLink = symbolicLink;
        this.size = size;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.path = path;
        
        if (directory) {
            children = new ArrayList<>();
        } else {
            children = null;
        }
    }

    /**
     *
     * @param file file to be added to the children list
     */
    public void addChild(FileInfo file) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (directory) {
            children.add(file);
            if (file.isDirectory()) {
                numberofDirectories++;
            } else {
                numberOfFiles++;
            }
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }

    @Override
    public String toString() {
        return name;
        // return "FileInfo{" + "name=" + name + ", directory=" + directory + ", symbolicLink=" + symbolicLink + ", size=" + size + ", creationTime=" + creationTime + ", lastAccessTime=" + lastAccessTime + ", lastModifiedTime=" + lastModifiedTime + '}';
    }

    /**
     *
     * @return name of the file or directory
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return if the instance is file or directory
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     *
     * @return if the instance is symbolic link
     */
    public boolean isSymbolicLink() {
        return symbolicLink;
    }

    /**
     * @throws IllegalStateException if the method is called on directory
     * @return return size of the file
     */
    public Long getSize() {
        if (directory) {
            throw new IllegalStateException("Size of folders is not calculated!");
        } else {
            return size;
        }
    }
    
    /**
     * 
     * @return creation time
     */
    public Date getCreationTime() {
        return new Date(creationTime.getTime());
    }

    /**
     * 
     * @return last access time
     */
    public Date getLastAccessTime() {
        return new Date(lastAccessTime.getTime());
    }

    /**
     * 
     * @return last modification time
     */
    public Date getLastModifiedTime() {
        return new Date(lastModifiedTime.getTime());
    }

    /**
     * 
     * @return number of files in the directory
     */
    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * 
     * @return number of subdirectories in the folder
     */
    public int getNumberOfDirectories() {
        return numberofDirectories;
    }

    /**
     * @throws IllegalStateException if called on a file
     * @return list of children of the directory
     */
    public List<FileInfo> getChildren() {
        if (directory) {
            return Collections.unmodifiableList(children);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }

    /**
     * Prints whole directory tree.
     * Mainly for testing purposes, can be removed in the final release.
     */
    public void print() {
        print(0);
    }

    /**
     * Recursively prints whole directory tree.
     * 
     * @param level indentation level
     */
    private void print(int level) {
        System.out.println(name);
        if (!directory) {
            return;
        }

        for (FileInfo file : children) {
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

    /**
     * Sorts children of the directory for purposes of the tree visualization 
     * and diff.
     * 
     * @throws IllegalStateException if called on a file
     */
    public void sortChildren() {
        if (directory) {
            Collections.sort(children);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }

    public ObservableList<Pair<String, String>> getPairedVariables() {

        ArrayList<Pair<String, String>> list = new ArrayList<>();

        list.add(new Pair("Name", name));

        if (!directory) {
            list.add(new Pair("Size", humanReadableByteCount(size, true) + " (" + size + " bytes)"));
        }

        list.add(new Pair("Creation Time", creationTime.toString()));
        list.add(new Pair("Last Access Time", lastAccessTime.toString()));
        list.add(new Pair("Creation Time", lastModifiedTime.toString()));
        list.add(new Pair("Symbolic Link", Boolean.toString(symbolicLink)));
        list.add(new Pair("Path", path));

        return FXCollections.observableArrayList(list);

    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
