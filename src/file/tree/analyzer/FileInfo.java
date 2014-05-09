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
    private final int numberOfFiles;
    private final int numberofDirectories;

    public FileInfo(Path file, BasicFileAttributes attributes) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }

        //needs special treatment with root
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
    
    public void addChild(FileInfo file) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (directory) {
            children.add(file);
        } else {
            throw new IllegalStateException("Files don't have children!");
        }
    }

    @Override
    public String toString() {
        return name;
        // return "FileInfo{" + "name=" + name + ", directory=" + directory + ", symbolicLink=" + symbolicLink + ", size=" + size + ", creationTime=" + creationTime + ", lastAccessTime=" + lastAccessTime + ", lastModifiedTime=" + lastModifiedTime + '}';
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

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getNumberOfDirectories() {
        return numberofDirectories;
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
