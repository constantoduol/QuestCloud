
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(19/6/12)
 */
public class NonExistentServiceException extends Exception {

    public NonExistentServiceException() {
         super("The specified service does not exist");
    }
    
}
