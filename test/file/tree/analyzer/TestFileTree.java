/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;

/**
 *
 * @author martina
 */
@Ignore
public class TestFileTree {

    /**
     * Creates file tree for testing.
     * @throws java.io.IOException
     */
    public static void createFileTree() throws IOException {
        File root = new File(".", "testDir");
        if (root.mkdir()) {
            File f1 = new File(root, "file1.txt");
            List<String> lines = Arrays.asList("test");
            Files.write(f1.toPath(), lines);

            File d = new File(root, "dir1");
            if (d.mkdir()) {
                File f2 = new File(d, "file2.txt");
                File f3 = new File(d, "file3.txt");
                f2.createNewFile();
                f3.createNewFile();
            }
            
            File d2 = new File(root, "dir2");
            d2.mkdir();
        }

    }

    /**
     * Deletes file tree that was created for testing.
     */
    public static void deleteFileTree() {
        File root = new File(".", "testDir");

        if (root.exists()) {
            File f = new File(root, "file1.txt");
            f.delete();

            File d = new File(root, "dir1");

            File f2 = new File(d, "file2.txt");
            f2.delete();

            File f3 = new File(d, "file3.txt");
            f3.delete();

            d.delete();

            File d2 = new File(root, "dir2");

            d2.delete();

            root.delete();
        }
    }
    
    /**
     * Creates changed version of the testing file structure.
     * Changes:
     * * file1.txt - changed size
     * * file3.txt - deleted
     * * newFile - new file
     * * newFolder - new folder
     * * newInnerFile - file in the newFolder
     * @throws IOException 
     */
    public static void createChangedFileTree() throws IOException {
        File root = new File(".", "changedTestDir");
        if (root.mkdir()) {
            File f1 = new File(root, "file1.txt");
            List<String> lines = Arrays.asList("test_changed");
            Files.write(f1.toPath(), lines);

            File d = new File(root, "dir1");
            if (d.mkdir()) {
                File f2 = new File(d, "file2.txt");
                f2.createNewFile();
            }
            
            File d2 = new File(root, "dir2");
            d2.mkdir();
            
            //new file
            File newFile = new File(root, "newFile");
            newFile.createNewFile();
            
            File newFolder = new File(root, "newFolder");
            newFolder.mkdir();
            
            File newInnerFile = new File(newFolder, "newInnerFile");
            newInnerFile.createNewFile();
        }
    }
    
    /**
     * Deletes changed testing file tree.
     */
    public static void deleteChangedFileTree() {
        File root = new File(".", "changedTestDir");

        if (root.exists()) {
            File f = new File(root, "file1.txt");
            f.delete();

            File d = new File(root, "dir1");

            File f2 = new File(d, "file2.txt");
            f2.delete();

            d.delete();

            File d2 = new File(root, "dir2");

            d2.delete();
            
            new File(root, "newFile").delete();
            
            File newFolder = new File(root, "newFolder");
            new File(newFolder, "newInnerFile").delete();
            newFolder.delete();
            
            root.delete();
        }
    }
}
