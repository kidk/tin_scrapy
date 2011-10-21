/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domein;

/**
 *
 * @author Thomas Van den Berge
 */

//Centraal aanspreek punt voor de GUI, Under Construction
public class DomeinController {
    
    private DownloadThread dt;
    private MainThread mt;
    private Queue q;
    
    
    public DomeinController() {
        dt = new DownloadThread();
        mt = new MainThread();
        q = new Queue();
    }
    
}
