/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
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

    private String name;
    private boolean directory;
    private boolean symbolicLink;
    private boolean accessibility = true;
    private Long size;
    private Date creationTime;
    private Date lastAccessTime;
    private Date lastModifiedTime;
    private List<FileInfo> children;
    private String path;
    private int numberOfFiles;
    private int numberofDirectories;
    private IOException exception;

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
        exception = null;
    }

    /**
     *
     * @param name name of file or directory
     * @param path path
     * @param directory is directory
     * @param symbolicLink is symbolic link
     * @param accessibility file accessibility
     * @param size size of file
     * @param creationTime creation time
     * @param lastAccessTime last access time
     * @param lastModifiedTime last modification time
     * @param children list of children
     * @param numberOfFiles number of files
     * @param numberofDirectories number of directories
     */
    public FileInfo(String name, String path, boolean directory, boolean symbolicLink,boolean accessibility, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories) {
        this.name = name;
        this.directory = directory;
        this.symbolicLink = symbolicLink;
        this.accessibility = accessibility;
        this.size = size;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.children = children;
        this.numberOfFiles = numberOfFiles;
        this.numberofDirectories = numberofDirectories;
        this.path = path;
        
    }

    FileInfo(Path file, IOException exc) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (exc == null) {
            throw new NullPointerException("exc");
        }
        
        //assuming we always get a valid path
        if (file.getFileName() == null) {
            name = file.getRoot().toString();
        } else {
            name = file.getFileName().toString();
        }
        this.path = file.toAbsolutePath().toString();
        directory = new File(path).isDirectory();
        this.exception = exc;
        
        if(exception != null){
            accessibility = false;
        }
    }
     
    /**
     * 
     * @return true if the file/folder is accessible
     */
    public boolean isAccessible() {
        return accessibility;
    }

    /**
     * 
     * @return exception of inaccessible file, or null if the file is accessible
     */
    public IOException getException() {
        return exception;
    }

//    /**
//     * Constructor designed for conversion from XML DOM.
//     *
//     * @param name name of file
//     * @param directory is directory
//     * @param symbolicLink is symbolic link
//     * @param size size
//     * @param creationTime creation time
//     * @param lastAccessTime last access time
//     * @param lastModifiedTime last modification time
//     * @param path full path
//     */
    /*public FileInfo(String name, boolean directory, boolean symbolicLink, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, String path) {
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
    }*/

    /**
     * Adds child and increments number of children 
     * files and directories (if necessary).
     *
     * @param file file to be added to the children list
     */
    public void addChild(FileInfo file) {
        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (directory) {
            children.add(file);
            if (file.isAccessible() && file.isDirectory()) {
                //don't forget +1 for the new directory
                numberofDirectories += file.getNumberOfDirectories() + 1;
                numberOfFiles += file.getNumberOfFiles();
            } else {
                numberOfFiles++;
            }
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }

    @Override
    public String toString() {
        return name;// + " files: " + numberOfFiles + " directories: " + numberofDirectories;
        // return "FileInfo{" + "name=" + name + ", directory=" + directory + ", symbolicLink=" + symbolicLink + ", size=" + size + ", creationTime=" + creationTime + ", lastAccessTime=" + lastAccessTime + ", lastModifiedTime=" + lastModifiedTime + '}';
    }
    public File toFile() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return new File(path);
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
     * @return path to the file or directory
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @return if the instance is file or directory
     */
    public boolean isDirectory() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return directory;
    }

    /**
     *
     * @return if the instance is symbolic link
     */
    public boolean isSymbolicLink() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return symbolicLink;
    }

    /**
     * @throws IllegalStateException if the method is called on directory
     * @return return size of the file
     */
    public Long getSize() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        if (directory) {
            throw new IllegalStateException("We do not calculate size of folders.");
        } else {
            return size;
        }
    }
    
    /**
     * 
     * @return creation time
     */
    public Date getCreationTime() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return new Date(creationTime.getTime());
    }

    /**
     * 
     * @return last access time
     */
    public Date getLastAccessTime() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return new Date(lastAccessTime.getTime());
    }

    /**
     * 
     * @return last modification time
     */
    public Date getLastModifiedTime() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return new Date(lastModifiedTime.getTime());
    }

    /**
     * 
     * @return number of files in the directory
     */
    public int getNumberOfFiles() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return numberOfFiles;
    }

    /**
     * 
     * @return number of subdirectories in the folder
     */
    public int getNumberOfDirectories() {
//        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        return numberofDirectories;
    }

    /**
     * @throws IllegalStateException if called on a file
     * @return list of children of the directory
     */
    public List<FileInfo> getChildren() {
        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        if (directory) {
            return Collections.unmodifiableList(children);
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
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
        System.out.println(toString());
        if (!directory || !this.isAccessible()) {
            return;
        }

        for (FileInfo file : children) {
            for (int i = 0; i <= level; i++) {
                System.out.print(" ");
            }
            file.print(level + 1);
        }
    }
    
    /**
     * Compares names of files like UNIX tool ls.
     * 
     * @param a first string to compare
     * @param b second string to compare
     * @return comparison of the names of two files
     */
    private int nameCompare(String a, String b) {
        boolean dotA = false;
        boolean dotB = false;
        
        if (a.startsWith(".") && !b.startsWith(".")) dotA = true;
        else if (!a.startsWith(".") && b.startsWith(".")) dotB = true;
        
        //removing dots from the beginning of the name
        if (dotA) {
            a = a.substring(1);
        }
        if (dotB) {
            b = b.substring(1);
        }
        
        int result = a.compareToIgnoreCase(b);
        if (result == 0) {
            result = -a.compareTo(b);
        }
        
        if (result == 0) {
            if (dotA) return 1;
            else if (dotB) return -1;
            else return result;
        } else {
            return result;
        }
    }

    /**
     * Sorts directories before files and then by names.
     * 
     * @param o file to compare
     * @return 
     */
    @Override
    public int compareTo(FileInfo o) {
        int accessCompare = -Boolean.compare(this.isAccessible(), o.isAccessible());
        if (accessCompare != 0) return accessCompare;
        
        int dirCompare = -Boolean.compare(directory, o.directory);
        if (dirCompare != 0) return dirCompare;
        
        return nameCompare(name, o.getName());
    }

    /**
     * Sorts children of the directory for purposes of the tree visualization 
     * and diff.
     * 
     * @throws IllegalStateException if called on a file
     */
    public void sortChildren() {
        if (!this.isAccessible()) throw new IllegalStateException("file is not accessible: " + path);
        
        if (directory) {
            Collections.sort(children);
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }

    public ObservableList<Pair<String, String>> getPairedVariables() {

        ArrayList<Pair<String, String>> list = new ArrayList<>();

        list.add(new Pair("Name", name));
        if (isAccessible()){
            
        
        if (!directory) {
            list.add(new Pair("Size", humanReadableByteCount(size, true) + " (" + size + " bytes)"));
        }else{
            list.add(new Pair("Number of Files", String.valueOf(numberOfFiles)));
            list.add(new Pair("Number of Directories",String.valueOf(numberofDirectories)));
        }
        
        list.add(new Pair("Creation Time", creationTime.toString()));
        list.add(new Pair("Last Access Time", lastAccessTime.toString()));
        list.add(new Pair("Creation Time", lastModifiedTime.toString()));
        list.add(new Pair("Symbolic Link", Boolean.toString(symbolicLink)));
        list.add(new Pair("Path", path));      
        }else{
            list.add(new Pair("Not Accessible", "true"));
        }

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
