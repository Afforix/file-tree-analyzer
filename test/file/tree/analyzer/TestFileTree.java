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

/**
 *
 * @author martina
 */
public class TestFileTree {
    
    /**
     * Creates file tree for testing.
     */
    public static void createFileTree() throws IOException {
        File root = new File(".", "testDir");
        if (root.mkdir()) {
            File f1 = new File(root, "file1.txt");
            List<String> lines = Arrays.asList("test");
            Files.write(f1.toPath(), lines);
            f1.createNewFile();

            File d = new File(root, "dir1");
            if (d.mkdir()) {
                File f2 = new File(d, "file2.txt");
                File f3 = new File(d, "file3.txt");
                f3.createNewFile();
                Files.createSymbolicLink(f2.toPath(), f3.toPath());
            }
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
            
            root.delete();
        }    
    }
}
