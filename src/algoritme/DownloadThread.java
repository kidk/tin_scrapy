package algoritme;

import java.net.URL;
import java.util.LinkedList;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThread implements Runnable {
    
    private LinkedList queue;

    DownloadThread(String website, LinkedList queue) {
        
    }
    
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public void run() {
        System.out.println("TEst");
        execute(new DownloadThread("http://www.google.be", queue));
    }
    
}
