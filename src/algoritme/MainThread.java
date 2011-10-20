package algoritme;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author samuelvandamme
 */
public class MainThread {

    private String website;
    private Integer threads;
    private final Queue queue = Queue.getInstance();
    private final List<PoolWorker> workers = new ArrayList();
    private Integer running = 0;
    
    public MainThread(String website) {
        this.website = website;
    }

    public void start() {
        
        // Eerste pagina toevoegen een work queue
        System.out.println("Eerste pagina");
        Queue.getInstance().add(new DownloadThread(website));
        
        // Threads aanmaken en aan het werk zetten
        System.out.println("Threads aanmaken");
        for(int i = 0; i < threads; i++) {
            workers.add(i, new PoolWorker(i));
            workers.get(i).start();
        }

    }
    
    
    
    private class PoolWorker extends Thread {

        private Integer id;
        
        private PoolWorker(int i) {
            this.id = i;
        }
        
        public void run() {
        
            Runnable r;
            
            while(!queue.isEmpty() || running > 0) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            System.out.println("Geen werk, wachten!");
                            queue.wait();
                        } catch (InterruptedException ignore) {
                            
                        }
                    }
                    
                    r = (Runnable) queue.removeFirst();
                }
                
                try {
                    System.out.println("Thread " + id + " is running.");
                    running++;
                    r.run(); 
                    System.out.println("Thread " + id + " is done.");
                    running--;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Thread " + id + " stopped. (" + queue.size() + ")");
            
        }
        
    }
    
    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    
}
