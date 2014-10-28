
package com.quest.access.useraccess;

/**
 *
 * @author constant oduol
 * @version 1.0(10/5/12)
 */

/**
 * This file defines an interface used by service providing classes
 * when a class implements this interface it means that it provides a
 * specific service to a client this interface forces the service classes
 * to implement the method provideService() which is called when a client makes a
 * request to a service. This interface is also implemented by dynamic proxies in
 * order to control access to a specific service. The provideService() method of a 
 * particular service is only invoked if the current user was assigned the permanent privilege
 * associated with that service
 */
public interface Serviceable {
    
    /**
     * this tells us whether the resource that is created from this interface is assignable
     * as a temporary privilege or not, a value set to true means that the resource can be 
     * assigned to a user as a temporary privilege while a value set to false means that
     * the resource cannot be assigned to users at runtime.
     */
    
    public final boolean isTemporary=false;
    
    
    /**
     * dummy method
     */
    public void service();
    
    /**
     * this method is meant to be used to provide a way of initialising what this service needs.
     * it is only called once when the service is created. This method can be used to create database tables
     * and other database objects that this service needs.
     */
    public void onCreate();
    
    /**
     * this method is called the first time this service is invoked by the server.
     * This method can be used to invoke database connectivity methods of this service
     */
    public void onStart();
}
