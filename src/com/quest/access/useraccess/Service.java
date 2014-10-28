
package com.quest.access.useraccess;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.quest.access.common.ExtensionClassLoader;
import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.verification.Action;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author constant oduol
 * @version 1.0(10/5/12)
 */


/**
 * <p>
 * This file defines a framework for providing services through a server
 * dividing the activities of a server into services splits up the servers
 * work and ensures that the logic of the different services is easily maintained
 * in different class files. When a programmer writes a service class he initializes
 * the service by creating an instance of the service class with the specified details.
 * When the server starts it reads all the services registered for this server into memory
 * and therefore whenever a client requests for any service, the server checks whether the service
 * exists, if it exists the server gets the location of the service class and then invokes the
 * provideService method in the specific service class.
 * </p>
 * <p>
 * Service providing classes must implement the Serviceable interface and provide an
 * implementation for the provideService() method. Services normally have a permanent 
 * privilege associated with them and therefore if a user has access to a service it
 * means he has the corresponding permanent privilege, revoking this privilege from
 * the user renders him unable to access the corresponding service. Every client's request
 * to a service runs in a different thread which invokes the specified service and returns the 
 * result to the client, this means that service classes should be designed with multi threading
 * in mind since different threads may call a service class at the same time
 * </p>
 * 
 */
public class Service {
    private static ConcurrentHashMap<String,Service> serviceCache=new ConcurrentHashMap();
    /**
     * this is the unique id of a service
     */
    
    private String serviceId;
    /**
     * this is the name of the service
     */
    private String name;
    /**
     * the permanent privilege associated with this service
     */
    private PermanentPrivilege priv;
    
    /**
     * this is the class that has the implementation of this service
     */
    private Class serviceClass;
    
    
    
    /**
     * this constructs a service object and stores the location and name of the service in the 
     * SERVICES table in the specific server's database
     * @param name the name of the service, this is the name the clients use when requesting for a
     *        service
     * @param serviceClass this is the class with the logic for the service so if a client makes a
     *        request for this service the code in this class is called
     * @param serv this is the server in whose scope this service is defined, therefore a different
     *        server cannot recognize this service because it does not belong to it
     */
   public Service(String name, Class serviceClass, Server serv,int level, Action action) throws ServiceExistsException{
	    Entity en = Datastore.getSingleEntity("SERVICES", "SERVICE_NAME", name,FilterOperator.EQUAL);
        if(en != null){
            // service already exists
            throw new ServiceExistsException();
         }
         UniqueRandom ur=new UniqueRandom(10);
         String serviceID=ur.nextRandom();
         this.serviceId=serviceID;
         this.serviceClass=serviceClass;
         Entity svc = new Entity("SERVICES");
         svc.setProperty("SERVICE_ID", serviceID);
         svc.setProperty("SERVICE_NAME",name);
         svc.setProperty("SERVICE_LOCATION",serviceClass.getName());
         svc.setProperty("CREATED",System.currentTimeMillis());
         svc.setProperty("ACTION_ID",action.getActionID());
         Datastore.insert(svc);
         action.saveAction();
         try {
        	 PermanentPrivilege privilege = new PermanentPrivilege(name, level,serv,action);
        	 this.priv=privilege;
        	 Resource res=new Resource(Serviceable.class);
        	 priv.initialize(new Resource[]{res});
        	 runOnCreate();
        } catch (Exception ex) {
          System.out.println(ex);     
        }
         //refresh this server's services
         serv.refreshServices();
         serv.startService(serviceClass.getName());
   }
   
   public void runOnCreate(){
        try {
            Method method = serviceClass.getMethod("onCreate", (Class[]) null);
            Object newInstance = serviceClass.newInstance();  
            method.invoke(newInstance, (Object[]) null);
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        } 
   }
   
   public void runOnStart(){
         try {
            Method method = serviceClass.getMethod("onStart", (Class[]) null);
            Object newInstance = serviceClass.newInstance();  
            method.invoke(newInstance, (Object[]) null);
        } catch (Exception ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }   
   }
   
   private Service(String name, PermanentPrivilege priv, Class clazz, String servId){
       this.name=name;
       this.priv=priv;
       this.serviceClass=clazz;
       this.serviceId=servId;
   }
   
   
   /**
    * this method returns the permanent privilege associated with a service
    */
   public PermanentPrivilege getServicePrivilege(){
       return this.priv;
   }
   
   
   /**
    * this method returns the unique system generated id for this service
    */
   
   public String getServiceId(){
       return this.serviceId;
   }
   /**
    * this method returns the class that has an implementation for this service
    */
   public Class getServiceClass(){
       return this.serviceClass;
   }
   /**
    * this method returns the name of this service
    */
   
   public String getServiceName(){
       return this.name;
   }
   
   

   

   
   /**
    * this method assigns a service to a groups of users
    * @param groups this represents the groups of users to be assigned this service
    * @param service this is the service to be assigned to the users
    * @param serv this is the server on which the service is accessed from
    */
   public static void assignServiceToGroups(ArrayList groups, Service service, Server serv){
       PermanentPrivilege.assignPrivilegeToGroups(groups,service.getServicePrivilege(), serv);
   }
   
   
   /**
    * this method returns an instance of an existing service
    * @param serviceName the name the service was given when it was created
    * @param serv the server the service was created in
    */
   
    public static Service getExistingService(String serviceName, Server serv) throws NonExistentServiceException {
      try{
    	if(serviceCache.containsKey(serviceName)){
    		return serviceCache.get(serviceName);
    	}
        Entity svc = Datastore.getSingleEntity("SERVICES", "SERVICE_NAME",serviceName,FilterOperator.EQUAL);
        if(svc == null){
        	throw new NonExistentServiceException();
        }
        String name = (String) svc.getProperty("SERVICE_NAME"); 
        String serviceLocation = (String) svc.getProperty("SERVICE_LOCATION");
        String serviceID = (String) svc.getProperty("SERVICE_ID");
        PermanentPrivilege privilege = PermanentPrivilege.getPrivilege(serviceName, serv);
        Class serviceClazz = Class.forName(serviceLocation);
        Service serviz=new Service(name, privilege, serviceClazz,serviceID);
        serviceCache.put(name, serviz);
        return serviz;
      }
      catch(Exception e){
    	  return null;
      }
      }
    
    /**
     * this method deletes a service completely and the privilege associated with it
     * and if any users were assigned this privilege the privilege is revoked
     * @param serviceName the name of the service to delete
     * @param serv the server the service was originally created in
     */
    public static void deleteService(String serviceName, Server serv) throws NonExistentPermanentPrivilegeException{
    	Datastore.deleteSingleEntity("SERVICES", "SERVICE_NAME", serviceName,FilterOperator.EQUAL);
    	PermanentPrivilege.deletePrivilege(serviceName, serv);
    	removeFromCache(serviceName);
    }
    
    /**
     * this method removes the service mapped by the specified service name
     * from the service cache
     * @param name the name of the service to be removed from the cache
     */
    public static void removeFromCache(String name){
        serviceCache.remove(name);
    }
    
    /**
     * this method empties all cached services
     */
    public static void emptyServiceCache(){
       serviceCache.clear();   
    }
    
    
   
   
   }
   

