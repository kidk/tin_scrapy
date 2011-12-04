package domein;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThread implements Runnable {

    private final Queue queue = Queue.getInstance();
    private String website;
    private String dir;
    private int currentDepth;
    private int maxDepth;
    private List<Email> emailList = new ArrayList<Email>();

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

    public int getCurrentDepth() {
        return currentDepth;
    }

    public void setCurrentDepth(int depth) {
        this.currentDepth = depth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int depth) {
        this.maxDepth = depth;
    }

    public DownloadThread() {
        
    }

    public DownloadThread(String website, String dir) {
        this(website, dir, 0, 0);
    }

    public DownloadThread(String website, String dir, int currentDepth, int maxDepth) {
        setWebsite(website);
        setDir(dir);
        setCurrentDepth(currentDepth);
        setMaxDepth(maxDepth);
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            if (!queue.contains(r)) {
                queue.addLast(r);
                System.out.println("Added " + getWebsite() + " to queue. (" + getDir() + ")");
            }
            queue.notify();
        }
    }

    @Override
    public void run() {
        Document doc = null;
        InputStream input = null;
        URI uri = null;

        // Debug
        System.out.println("Fetching " + website);

        String fileLok = dir + ((currentDepth > 0) ? getLocalFileName(getPathWithFilename(website)) : "index.html");

        // Bestaat lokaal bestand
        File bestand = new File(fileLok);

        if (bestand.exists()) {
            return;
        }

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
                doc = Jsoup.parse(input, null, getBaseUrl(website));
            } catch (IOException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, website + " niet afgehaald.", ex);
                return;
            }

            // base tags moeten leeg zijn
            doc.getElementsByTag("base").remove();

            // Script tags leeg maken => krijgen veel te veel errors => soms blijven pagina's hangen hierdoor
            doc.getElementsByTag("script").remove();

            // Afbeeldingen 
            for (Element image : doc.getElementsByTag("img")) {

                // Afbeelding ophalen
                addToQueue(getPath(image.attr("src")));

                // Afbeelding zijn source vervangen door een MD5 hash 
                image.attr("src", getLocalFileName(getPathWithFilename(image.attr("src"))));

            }

            // CSS bestanden
            for (Element cssFile : doc.getElementsByTag("link")) {
                
                if(cssFile.attr("rel").equals("stylesheet")) {

                    // CSS bestand ophalen
                    addToQueue(getPath(cssFile.attr("href")));

                    // CSS bestand zijn verwijziging vervangen door een MD5 hash
                    cssFile.attr("href", getLocalFileName(getPathWithFilename(cssFile.attr("href"))));
                                       
                }
                
            }

            // Links overlopen
            for (Element link : doc.getElementsByTag("a")) {

                if (link.attr("href").contains("#") || link.attr("href").startsWith("ftp://")) {
                    continue;
                }

                // Link toevoegen 
                if (!(link.attr("href")).contains("mailto")) {

                    if(link.attr("href").equals(".")) {
                        link.attr("href", "index.html");
                        continue;
                    }
                        
                    if(link.attr("href").startsWith("http") && isExternal(link.attr("href"), website))
                    {
                        addExternalLink(link.attr("href"));
                    }
                    else
                    {
                        if(maxDepth > 0 && currentDepth >= maxDepth)
                            continue;

                        addToQueue(getPath(link.attr("href")));

                        link.attr("href", getLocalFileName(getPathWithFilename(getPath(link.attr("href")))));
                    }
                }
                else if ((link.attr("href")).contains("mailto")) {
                    addEmail(link.attr("href").replace("mailto:", ""));
                }
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
        execute(new DownloadThread(url, dir, currentDepth + 1, maxDepth));
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

        String result = "fail";

        try {

            // Indien het url niet start met http, https of ftp er het huidige url voorplakken
            if(!url.startsWith("http") && !url.startsWith("https") && !url.startsWith("ftp"))
            {
                // Indien het url start met '/', eruithalen, anders krijgen we bijvoorbeeld http://www.hln.be//Page/14/01/2011/...
                url = getBaseUrl(website) + (url.startsWith("/") ? url.substring(1) : url);
            }

            URI path = new URI(url.replace(" ", "%20")); // Redelijk hacky, zou een betere oplossing voor moeten zijn

            result = path.toString();

        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
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
            String host = "http://" + path.toURL().getHost();
            return host.endsWith("/") ? host : (host + "/");
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
            result = ((URLConnection) uri.openConnection()).getContentType();
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

    private void addEmail(String mail) {
        Email email = new Email(mail);
        emailList.add(email);
        System.out.println("Email found and added: " + email.getAddress());
    }

    private void addExternalLink(String link) {
        ExternalLink elink = new ExternalLink(link);
        System.out.println("External Link found and added. link: " + link);
    }

    public boolean isExternal(String attr, String website) {

        URI check = null;
        URI source = null;
        try {
            check = new URI(attr);
            source = new URI(website);
        } catch (URISyntaxException ex) {
            return true;
        }
        
        if ( check.getHost().equals(source.getHost()))
            return false;
        
        
        return true;       
    }

    public String getLocalFileName(String name)
    {
        try {
            byte[] bytesOfMessage = name.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            String extension = getExtension(name);
            return new BigInteger(1,thedigest).toString(16) + ("".equals(extension) ? "" : "." + extension);
        } catch (Exception ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "0";
    }

    public String getExtension(String url)
    {
        // Haal de extensie uit het URL
        // We starten altijd met html
        String extension = "html";

        // Indien een website gebruik maakt van ? in het url, bv, http://www.google.be/test.html?a=152&b=535
        // split op het vraagteken en gebruik de linker helft.
        url = url.split("\\?")[0];
        
        if(url.contains(".")) {

            int mid = url.lastIndexOf(".");

            extension = url.substring(mid + 1, url.length());

            // Enkele extensies willen we vervangen + indien het resultaat eindigt
            // op een / of \ wil het zeggen dat het url bijvoorbeeld http://www.google.com/nieuws/ was.
            // De extensie is dan gewoon .html 
            if(extension.equals("php") || extension.equals("dhtml") || extension.equals("aspx") || extension.equals("asp") ||
               extension.contains("\\") || extension.contains("/")){                
                extension = "html";                
            }            
        }
        return extension;
    }
}
