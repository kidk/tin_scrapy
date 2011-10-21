/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algoritme;

import domein.DownloadThread;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThreadTest {
    DownloadThread instance;


    
    @Before
    public void setUp() {
        instance = new DownloadThread("", "");
    }

    /**
     * Test of getPath method, of class DownloadThread.
     */
    @Test
    public void testGetPath() {  
        assertEquals(instance.getPath("http://www.google.be/"), "");
        assertEquals(instance.getPath("http://www.google.be/test/index.html"), "test/index.html");
        assertEquals(instance.getPath("http://www.google.be/index.html"), "index.html");
    }
    
    @Test
    public void testGetBaseUrl() { 
        assertEquals(instance.getBaseUrl("http://www.google.be/"), "http://www.google.be/");
        assertEquals(instance.getBaseUrl("http://www.google.be/test/index.html"), "http://www.google.be/");
        assertEquals(instance.getBaseUrl("http://www.google.be/index.html"), "http://www.google.be/");
    }
    
    @Test
    public void testGetMimeType() {
        assertEquals(instance.getMimeType("http://www.google.be/"), "text/html");
        assertEquals(instance.getMimeType("http://www.google.be/test/index.html"), "text/html");
        assertEquals(instance.getMimeType("http://www.google.be/index.html"), "text/html");
    }
    
    @Test
    public void testgetPathWithFilename() {    
        assertEquals(instance.getPathWithFilename("http://www.google.be/"), "index.html");
        assertEquals(instance.getPathWithFilename("http://www.google.be/test/index.html"), "test/index.html");
        assertEquals(instance.getPathWithFilename("http://www.google.be/index.html"), "index.html");
        assertEquals(instance.getPathWithFilename("http://www.google.be/google.png"), "google.png");
    }
}