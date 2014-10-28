
package com.quest.access.useraccess;

/**
 *This exception is thrown if a service attempts to be recreated when it already exists
 * @author Conny
 */
public class ServiceExistsException extends Exception {

    public ServiceExistsException() {
        super("Specified service already exists");
    }
    
}
