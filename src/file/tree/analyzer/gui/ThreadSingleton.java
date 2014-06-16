package file.tree.analyzer.gui;

/**
 *
 * @author ansy
 */
public class ThreadSingleton {

    private static ThreadSingleton instance;
    private Thread  diskExplorerThread;
    
    protected ThreadSingleton() {
        // Exists only to defeat instantiation.
    }
    
    public static ThreadSingleton getInstance() {
        if(instance == null) {
         instance = new ThreadSingleton();
      }
        return instance;
    }
    
    public Thread getDiskExplorerThread() {
        return diskExplorerThread;
    }

    public void setDiskExplorerThread(Thread diskExplorerTask) {
        this.diskExplorerThread = diskExplorerTask;
    }

}
