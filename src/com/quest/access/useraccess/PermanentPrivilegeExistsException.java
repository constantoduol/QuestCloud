
package com.quest.access.useraccess;

/**
 *This exception is thrown when a permanent privilege that already exists tries to be recreated
 * @author Conny
 */
public class PermanentPrivilegeExistsException extends Exception {
    public PermanentPrivilegeExistsException() {
       super("The specified privilege already exists");
    }
    
}
