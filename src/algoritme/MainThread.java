package algoritme;

import java.net.URL;
import java.util.LinkedList;

/**
 *
 * @author samuelvandamme
 */
public class MainThread {

    private URL website;
    private Integer threads;
    private final LinkedList queue = new LinkedList();
    private final PoolWorker[] workers = new PoolWorker[threads];
    
    public MainThread(String website) {
        
    }

    public void start() {
        
        for(int i = 0; i < threads; i++) {
            workers[i] = new PoolWorker();
            workers[i].start();
        }
        
        
    }
    
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }
    
    private class PoolWorker extends Thread {
        
        public void run() {
        
            Runnable r;
            
            while(true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignore) {
                            
                        }
                    }
                    
                    r = (Runnable) queue.removeFirst();
                }
                
                try {
                    r.run();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
    }
    
    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public URL getWebsite() {
        return website;
    }

    public void setWebsite(URL website) {
        this.website = website;
    }
    
}
