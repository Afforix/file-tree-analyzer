/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package file.tree.analyzer;

/**
 *
 * @author ansy
 */
public class FileTreeAnalyzer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Prints contents of the given directory:");
        DiscExplorer explorer = new DiscExplorer();
        explorer.listFiles(".");
        System.out.println("DONE");
    }
}
