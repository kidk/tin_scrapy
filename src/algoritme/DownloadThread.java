package algoritme;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThread implements Runnable {

    private final Queue queue = Queue.getInstance();
    private String website;

    DownloadThread(String website) {
        this.website = website;
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public void run() {
        // Pagina afhalen
        Document doc = null;
        System.out.println("starting " + website);
        System.out.println("download");
        
        // Mimetype checking
        // hier zit ik momenteel effe vast, we hebben goeie mimetype detectie nodig om te zien wat we moeten downloaden en wat niet
        // check functie beneden
        
        try {
            doc = Jsoup.connect(website).get();
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, website + " niet afgehaald.", ex);
            return;
        }

        // Afbeeldingen / CSS / Javascript / Flash / ... afhalen
        System.out.println("images");
        Elements images = doc.getElementsByTag("img");
        for(Element image : images)
            execute(new DownloadThread(getBaseUrl(website) + getPath(image.attr("src"))));

        // Links afhalen en opslaan in queue
        // Opslaan in queue : execute(new DownloadThread(Url van pagina)

        // Links aanpassen zodat ze lokaal werken

        // Pagina opslaan
    }
    
    public String getPath(String url) {
        // Path ophalen
        String result = "fail";
        try {
            URI path = new URI(url);
            result = path.getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (result.charAt(0) != '/')
            result = "/" + result;
        
        return result;
    }
    
    public String getBaseUrl(String url) {
        // Path ophalen
        try {
            URI path = new URI(url);
            return "http://" + path.toURL().getHost();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "fail";
    }
    
    //       ##
    //       ##
    //       ##
    //       ##
    //       ##
    //   ##########
    //     ######
    //       ##
    public String getMimeType(String url) {
        return new MimetypesFileTypeMap().getContentType(url);
    }
}
