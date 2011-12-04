package domein;

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
    private String dir;
    private Integer depth = 0;
    private Integer maxDepth = 0;
    
    //Efkes empty constructor
    public MainThread() {
        
    }
    public MainThread(String website) {
        this.website = website;
    }

    public void start() {
        
        // Eerste pagina toevoegen een work queue
        System.out.println("Eerste pagina");
        Queue.getInstance().add(new DownloadThread(website, dir, getDepth(), getMaxDepth()));

        // Threads aanmaken en aan het werk zetten
        System.out.println("Threads aanmaken");
        for (int i = 0; i < threads; i++) {
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

            Runnable r = null;

            while (!queue.isEmpty() || running > 0) {
                synchronized (queue) {
                    while (queue.isEmpty() && running > 0) {
                        try {
                            System.out.println("Thread " + id + " waiting");
                            queue.wait();
                        } catch (InterruptedException ignore) {
                        }
                    }

                    if (!queue.isEmpty()) {
                        r = (Runnable) queue.removeFirst();
                    } 
                }

                if (r != null) {
                    try {
                        System.out.println("Thread " + id + " is running a job." + "[" + running + "]");
                        running++;
                        
                        // Run job
                        r.run();
                        System.out.println("Thread " + id + " is done with a job." + "[" + running + "]");
                        
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    finally
                    {
                        running--;
                        r = null;
                    }
                }
            }

            // Trigger threads
            synchronized (queue) {
                queue.notifyAll();
            }
            
            System.out.println("Thread " + id + " stopped. (" + queue.size() + ")" + "[" + running + "]");

        }
    }

    public void stopThreads(){
        queue.clear();
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
    
    public void setDataDir(String dir) {

        if(dir.endsWith("/") || dir.endsWith("\\"))
            dir = dir + "\\";

        this.dir = dir;
        
    }
    
    public String getDataDir() {
        return dir;
    }
 
    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }
}   

