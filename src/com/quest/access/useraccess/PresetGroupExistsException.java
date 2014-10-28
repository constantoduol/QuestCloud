
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(18/6/12)
 */
public class PresetGroupExistsException extends Exception {
    public PresetGroupExistsException() {
        super("The specified preset group already exists");
    }
    
}
