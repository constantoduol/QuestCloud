

package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(3/5/12)
 */

/**
 * This file defines an exception this exception is thrown when a call to
 * @see PermanentPrivilege#getExistingPermanentPrivilege(java.lang.String, com.qaccess.net.Server) 
 * is made and the specified privilege is found to be non existent
 */
public class NonExistentPermanentPrivilegeException  extends Exception{
    public NonExistentPermanentPrivilegeException(){
        super("The specified permanent privilege does not exist");
    }
}
