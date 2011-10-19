package algoritme;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
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
        Document doc = null;
        InputStream input = null;
        URI uri = null;

        // Debug
        System.out.println("Fetching " + website);
        
        // Bestand ophalen
        try {
            uri = new URI(website);
            input = uri.toURL().openStream();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Type controleren
        String type = getMimeType(website);

        if (type.equals("text/html")) {
            // HTML Parsen
            try {
                doc = Jsoup.parse(input, "UTF-8", getBaseUrl(website));
            } catch (IOException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, website + " niet afgehaald.", ex);
                return;
            }

            // Afbeeldingen / CSS / Javascript / Flash / ... afhalen
            System.out.println("images");
            Elements images = doc.getElementsByTag("img");
            for(Element image : images)
                execute(new DownloadThread(getBaseUrl(website) + getPath(image.attr("src"))));
        }
        
        File bestand = new File(getFileName(website)); 
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

        if (result.charAt(0) != '/') {
            result = "/" + result;
        }

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

    public String getMimeType(String url) {
        URL uri = null;
        String result = "";
        try {
            uri = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            result = ((HttpURLConnection) uri.openConnection()).getContentType();
            if (result.indexOf(";") != -1)
                return result.substring(0, result.indexOf(";"));
            else
                return result;
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            return "text/unknown";
        }
    }
    
    public String getFileName(String url) {
        return "niet af";
    }
    
}
