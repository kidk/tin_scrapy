/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domein;

import java.util.LinkedList;

/**
 *
 * @author samuelvandamme
 */
public class Queue extends LinkedList<Runnable> {
    
    private static Queue object = null;
    
    public static Queue getInstance() {
        if (object == null)
            object = new Queue();
        
        return object;
    }
    
}
