package algoritme;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThread implements Runnable {
    
    private final Queue queue = Queue.getInstance();

    DownloadThread(String website) {
        
    }
    
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public void run() {
        // Pagina afhalen
        
        // Afbeeldingen afhalen
        
        // Links afhalen en opslaan in queue
        // Opslaan in queue : execute(new DownloadThread(Url van pagina)
        
        // Links aanpassen zodat ze lokaal werken
        
        // Pagina opslaan
        execute(new DownloadThread("http://www.google.be"));
    }
    
}
