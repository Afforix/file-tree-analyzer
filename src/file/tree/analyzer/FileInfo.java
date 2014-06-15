package file.tree.analyzer;

import file.tree.analyzer.gui.RowInfo;
import file.tree.analyzer.utils.Utils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Entity class for storing information about file or directory. In case of
 * directory, all children are accessible using instance of this class.
 *
 * @author afforix
 */
public class FileInfo implements Comparable<FileInfo> {

    protected String name;
    protected boolean directory;
    protected boolean symbolicLink;
    protected boolean accessibility;
    protected Long size;
    protected Date creationTime;
    protected Date lastAccessTime;
    protected Date lastModifiedTime;
    protected List<FileInfo> children;
    protected String path;
    protected int numberOfFiles;
    protected int numberOfDirectories;

    /**
     * Constructor designed to use during hard disk analysis.
     * 
     * @throws NullPointerException if parameter file or attributes is null
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

        resolvePath(file);
        symbolicLink = attributes.isSymbolicLink();
        directory = Files.isDirectory(file);

        if (!directory) {
            size = attributes.size();
        }

        creationTime = new Date(attributes.creationTime().toMillis());
        lastAccessTime = new Date(attributes.lastAccessTime().toMillis());
        lastModifiedTime = new Date(attributes.lastModifiedTime().toMillis());
        
        accessibility = true;
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
     * @deprecated
     */
    @Deprecated
    public FileInfo(String name, String path, boolean directory, boolean symbolicLink, boolean accessibility, Long size, Date creationTime, Date lastAccessTime, Date lastModifiedTime, List<FileInfo> children, int numberOfFiles, int numberofDirectories) {
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
        this.numberOfDirectories = numberofDirectories;
        this.path = path;

    }

    /**
     * Standard non-parametric constructor.
     */
    public FileInfo() {
    }

    /**
     * Constructor for cases when we only know path to the file.
     * Tries to set name, path, and directory attributes, accesibility is set 
     * to false (this means that the file/folder is not readable).
     * 
     * @param file path to the file/directory
     */
    public FileInfo(Path file) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        resolvePath(file);
        directory = Files.isDirectory(file);
        accessibility = false;
    }

    /**
     * Sets name and path.
     * 
     * @param filePath path to resolve
     */
    private void resolvePath(Path filePath) {
        //needs special treatment with root, because root does not have  
        //a regular file name, so it's necessary to obtain root name
        if (filePath.getFileName() == null) {
            name = filePath.getRoot().toString();
        } else {
            name = filePath.getFileName().toString();
        }

        path = filePath.toAbsolutePath().toString();
    }

    /**
     *
     * @return true if the file/folder is accessible
     */
    public boolean isAccessible() {
        return accessibility;
    }

    /**
     * Adds child and increments number of children files and directories (if
     * necessary).
     *
     * @param child file to be added to the children list
     */
    public void addChildAndCount(FileInfo child) {
        if (child == null) {
            throw new NullPointerException("file");
        }

        addChild(child);

        if (child.isDirectory()) {
            //don't forget +1 for the new directory
            numberOfDirectories += child.getNumberOfDirectories() + 1;
            numberOfFiles += child.getNumberOfFiles();
        } else {
            numberOfFiles++;
        }
    }

    /**
     * Adds child without calculating number of files and subdirectories.
     * 
     * @param child file to be added to the children list
     */
    public void addChild(FileInfo child) {
        if (directory) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(child);
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public File toFile() {
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
        return size;
    }

    /**
     *
     * @return creation time
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     *
     * @return last access time
     */
    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     *
     * @return last modification time
     */
    public Date getLastModifiedTime() {
        return lastModifiedTime;
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
        return numberOfDirectories;
    }

    /**
     * @throws IllegalStateException if called on a file
     * @return list of children of the directory
     */
    public List<FileInfo> getChildren() {
        if (directory) {
            if (children == null) {
                return Collections.emptyList();
            } else {
                return Collections.unmodifiableList(children);
            }
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }

    /**
     * Prints whole directory tree.
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

        if (a.startsWith(".") && !b.startsWith(".")) {
            dotA = true;
        } else if (!a.startsWith(".") && b.startsWith(".")) {
            dotB = true;
        }

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
            if (dotA) {
                return 1;
            } else if (dotB) {
                return -1;
            } else {
                return result;
            }
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
        int dirCompare = -Boolean.compare(directory, o.directory);
        if (dirCompare != 0) {
            return dirCompare;
        }

        return nameCompare(name, o.getName());
    }

    /**
     * Sorts children of the directory for purposes of the tree visualization
     * and diff.
     *
     * @throws IllegalStateException if called on a file
     */
    public void sortChildren() {
        if (!this.isAccessible()) {
            throw new IllegalStateException("file is not accessible: " + path);
        }
        
        if (children == null) return;

        if (directory) {
            Collections.sort(children);
        } else {
            throw new IllegalStateException("Files don't have children! " + path);
        }
    }

    /**
     * 
     * @return information about file in a form of rows for table
     */
    public ObservableList<RowInfo> getTableRows() {

        ArrayList<RowInfo> list = new ArrayList<>();

        list.add(new RowInfo("Name", name));
        if (isAccessible()) {

            if (!directory) {
                list.add(new RowInfo("Size", Utils.humanReadableByteCount(size, true) + " (" + size + " bytes)"));
            } else {
                list.add(new RowInfo("Number of Files", String.valueOf(numberOfFiles)));
                list.add(new RowInfo("Number of Directories", String.valueOf(numberOfDirectories)));
            }

            list.add(new RowInfo("Creation Time", creationTime.toString()));
            list.add(new RowInfo("Last Access Time", lastAccessTime.toString()));
            list.add(new RowInfo("Last Modification Time", lastModifiedTime.toString()));
            list.add(new RowInfo("Symbolic Link", Boolean.toString(symbolicLink)));
            list.add(new RowInfo("Path", path));
        } else {
            list.add(new RowInfo("Accessible", "false"));
        }

        return FXCollections.observableArrayList(list);

    }


    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param directory if the record is a directory
     */
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    /**
     *
     * @param symbolicLink if the record is symbolic link
     */
    public void setSymbolicLink(boolean symbolicLink) {
        this.symbolicLink = symbolicLink;
    }

    /**
     *
     * @param accessibility if the record is accesible (readable) file/folder
     */
    public void setAccessibility(boolean accessibility) {
        this.accessibility = accessibility;
    }

    /**
     *
     * @param size
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     *
     * @param creationTime
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     *
     * @param lastAccessTime
     */
    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    /**
     *
     * @param lastModifiedTime
     */
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @param numberOfFiles
     */
    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    /**
     *
     * @param numberOfDirectories
     */
    public void setNumberOfDirectories(int numberOfDirectories) {
        this.numberOfDirectories = numberOfDirectories;
    }

}
