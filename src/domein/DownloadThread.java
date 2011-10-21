package domein;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    
    //Efkes emtpy constructor, wordt nog gewijzigd
    DownloadThread() {
    }
    

    public DownloadThread(String website, String dir) {
        setWebsite(website);
        setDir(dir);
        System.out.println("Added " + website + " to queue. (" + dir + ")");
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    @Override
    public void run() {
        Document doc = null;
        InputStream input = null;
        BufferedReader bfrd = null;
        URI uri = null;

        // Debug
        System.out.println("Fetching " + website);

        // Bestaat lokaal bestand
        File bestand = new File(getPathWithFilename(website));
        if (bestand.exists())
            return;
        
        // Bestand ophalen
        try {
            uri = new URI(website);
            input = uri.toURL().openStream();
            bfrd = new BufferedReader(new InputStreamReader(input));
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
                doc = Jsoup.parse(input, "UTF-8", getBaseUrl(website)); // Codering = fout
            } catch (IOException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, website + " niet afgehaald.", ex);
                return;
            }

            // Afbeeldingen / CSS / Javascript / Flash / ... afhalen
            Elements images = doc.getElementsByTag("img");
            for (Element image : images) {
                addToQueue(getBaseUrl(website) + getPath(image.attr("src")));
            }
            
            Elements links = doc.getElementsByTag("a");
            for (Element link : links) {
                addToQueue(getBaseUrl(website) + getPath(link.attr("href")));
            }
            
        }

        System.out.println("Save to " + bestand.getAbsolutePath());
        createFile(bestand);

        // Save
        if (type.equals("text/html")) {
            saveHtml(bestand, doc.html());
        } else {
            saveBinary(bestand, input);
        }

        // Close
        try {
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addToQueue(String url) {
        File bestand = new File(getPathWithFilename(url));
        if (!bestand.exists())
            execute(new DownloadThread(url, dir));
    }

    public void createFile(File bestand) {
        // Maak bestand en dir's aan
        try {
            if (bestand.getParentFile() != null) {
                bestand.getParentFile().mkdirs();
            }
            System.out.println("Path " + bestand.getAbsolutePath());
            bestand.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveHtml(File bestand, String html) {
        // Open bestand
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(bestand));
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            output.write(html);
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }


        // Close it
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveBinary(File bestand, InputStream input) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(bestand);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] buffer = new byte[4096];
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            input.close();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getPath(String url) {
        // Path ophalen
        String result = "fail";
        try {
            URI path = new URI(url.replace(" ", "%20")); // Redelijk hacky, zou een betere oplossing voor moeten zijn
            result = path.toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result.charAt(0) == '/') {
            result = result.substring(1);
        }

        return result;
    }

    public String getPathWithFilename(String url) {
        String result = getPath(url);  
        result = result.replace("http://", "");
        if ((result.length() > 1 && result.charAt(result.length() - 1) == '/') || result.length() == 0) {
            return dir + result + "index.html";
        } else {
            return dir + result;
        }
    }

    public String getBaseUrl(String url) {
        // Path ophalen
        try {
            URI path = new URI(url);
            return "http://" + path.toURL().getHost() + "/";
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
            if (result.indexOf(";") != -1) {
                return result.substring(0, result.indexOf(";"));
            } else {
                return result;
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            return "text/unknown";
        }
    }
}
