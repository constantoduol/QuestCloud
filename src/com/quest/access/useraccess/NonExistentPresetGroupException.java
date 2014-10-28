
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(19.6.12)
 */
public class NonExistentPresetGroupException extends Exception {

    public NonExistentPresetGroupException() {
        super("The specified preset group does not exist");
    }
    
}
