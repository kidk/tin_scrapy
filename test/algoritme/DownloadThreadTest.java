/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algoritme;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author samuelvandamme
 */
public class DownloadThreadTest {
    
    public DownloadThreadTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getPath method, of class DownloadThread.
     */
    @Test
    public void testGetPath() {
        DownloadThread instance = new DownloadThread("");
        
        assertEquals(instance.getPath("http://www.google.be/"), "/");
        assertEquals(instance.getPath("http://www.google.be/test/index.html"), "/test/index.html");
        assertEquals(instance.getPath("http://www.google.be/index.html"), "/index.html");
    }
    
    @Test
    public void testGetBaseUrl() {
        DownloadThread instance = new DownloadThread("");
        
        assertEquals(instance.getBaseUrl("http://www.google.be/"), "http://www.google.be");
        assertEquals(instance.getBaseUrl("http://www.google.be/test/index.html"), "http://www.google.be");
        assertEquals(instance.getBaseUrl("http://www.google.be/index.html"), "http://www.google.be");
    }
    
    @Test
    public void testGetMimeType() {
        DownloadThread instance = new DownloadThread("");
        
        assertEquals(instance.getMimeType("http://www.google.be/"), "text/html");
        assertEquals(instance.getMimeType("http://www.google.be/test/index.html"), "text/html");
        assertEquals(instance.getMimeType("http://www.google.be/index.html"), "text/html");
    }
}
