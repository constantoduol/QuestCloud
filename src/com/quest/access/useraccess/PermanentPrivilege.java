
package com.quest.access.useraccess;


import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.verification.Action;
import com.quest.access.useraccess.verification.UserAction;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author constant oduol
 * @version 1.0(4/1/2012)
 */

/**
 * <p>
 * A permanent privilege is a privilege that can be assigned to 
 * a user over a long period of time without being revoked. Permanent privileges
 * define what a user has access to and what  a user does not have access to.
 * </p>
 * <p>
 * Permanent privileges are created by specifying the name of the privilege, the level of the privilege
 * (This is how superior this privilege is to others, developers can have there own measure of
 * superiority, a simple way would be to say the least superior privilege has a level of 1 while
 * the most superior has a level of 10) and the server in which this privilege is defined.
 * When these privileges are created they are stored in a table called RESOURCE_GROUPS in the
 * server's database, each privilege has a unique id which is used when assigning a privilege to
 * a user
 * </p>
 * <p>
 * Permanent privileges have resources associated with them. Every permanent privilege
 * has a number of resources associated with it, therefore if a user has access to a given permanent
 * privilege then he has access to all the resources under that permanent privilege.Normally the user's 
 * permanent privileges are retrieved at login time and stored in his or her session such that the server's 
 * privilegeHandler is able to determine whether a user should access a server resource or not. When a user
 * tries to access a permanent privilege that he has no access to a SecurityException is thrown by the server
 * Privileges can be loaded to a server via an xml file
 * </p>
 * @see com.qaccess.useraccess.Resource
 */

public class PermanentPrivilege extends ResourceGroup {
    /**
     * cache for permanent privileges
     */
    private static ConcurrentHashMap<String, PermanentPrivilege> privilegeCache = new ConcurrentHashMap();
    /**
     * this is the unique system generated id for this privilege
     */
    private String privilegeID;
    /**
     * this tells us whether the resources in this permanent privilege can be accessed or not
     */
    private boolean isAccessible;
    
    private Server serv;
    
    private Date created;
    /**
     * creates a permanent privilege object that is accessible by users who have been
     * assigned this privilege
     * @param name the name of the permanent privilege 
     * @param level this shows how superior this privilege is in comparison
     *              to other privileges
     * @param serv  the server in which this privilege is to be created
     */
    public PermanentPrivilege(String name, int level, Server serv, Action action) throws PermanentPrivilegeExistsException{
        super(name,level);
        this.isAccessible=true;
        this.serv=serv;
        savePrivilege(this,action);
    }
    
    /**
     * creates a permanent privilege object while allowing the ability to 
     * specify whether the privilege is accessible or not
     * @param name the name of the privilege to be created
     * @param level this shows how superior this privilege is in comparison to
     *             other privileges
     * @param serv the server in which  this privilege is to be created
     * @param accessState tells us whether the privilege is accessible or not
     */
    private PermanentPrivilege(String name,int level, Server serv, boolean accessState, String privID, Date created){
        super(name,level);
        this.isAccessible=accessState;
        this.serv=serv;
        this.privilegeID=privID;
        this.created=created;
    }
    
    
    
    /**
     * this method initializes resources as belonging to a specific permanent privilege
     * @param group the permanent privilege we want to initialize with the specific resources
     * @param res the resources we want to set as belonging to this permanent privilege
     * @param serv the server in which this permanent privilege belongs
     * @return returns a permanent privilege object with the resources belonging to it
     *         having been saved
     */
    public static PermanentPrivilege initialize(PermanentPrivilege group, Resource[] res, Server serv){
         group.addManyResources(res);
         saveResources(group, serv);
         return group;
    }
    
    /**
     *this method changes the access state of this permanent privilege to either true or false
     * this determines whether this privilege is accessible or not
     * @param state if state is true then it means that access to this privilege is allowed
     *              otherwise if it is false then access to the privilege will be denied
     */
    public void setAccessState(boolean state){
      Integer toSet = state == true ? 1 : 0;
      // if someone is trying to disable this privilege then this
      // will force the server to refresh the privileges details in the database
      // and a call to getPrivilege() will cause a database read
       if(state==false)
         privilegeCache.remove(serv); 
       Datastore.updateSingleEntity("RESOURCE_GROUPS","NAME",this.getName(),"ACCESS_STATE",toSet.toString(),FilterOperator.EQUAL);
       this.isAccessible=state;
    }
    
    /**
     * this method tells us whether this permanent privilege is accessible or not
     * it returns true if the permanent privilege is accessible and false if it is not
     */
    public boolean getAccessState(){
        return this.isAccessible;
    }
    
    /**
     * this method saves the resources specified under this permanent privilege
     * @param res the array of resources we want to initialize under this privilege
     */
    public PermanentPrivilege initialize(Resource [] res){
        this.addManyResources(res);
        saveResources(this, this.serv);
        return this;
    }
    
    /**
     * this method returns an instance of an existing privilege from the
     * specified server
     * @param privName the name of the privilege we want to get an instance of
     * @param serv the server this privilege is found
     * @return an instance of the existing privilege
     */
    public static PermanentPrivilege getPrivilege(String privName, Server serv) throws NonExistentPermanentPrivilegeException{
       boolean containsKey = privilegeCache.containsKey(privName);
       if(!containsKey){
    	   return PermanentPrivilege.getExistingPermanentPrivilege(privName, serv); // i need to get an existing permanent privilege
    	 }
       else{
          return (PermanentPrivilege)privilegeCache.get(privName);
        }
    }
    
    /**
     * this method initializes the permanent privilege specified by group by creating
     * new resource objects from the array of classes, this resources are then saved as being under
     * the permanent privilege specified by group
     * @param group this is the permanent privilege that is being initialized with the resource objects
     *              created from the class objects specified by cls
     * @param cls   this is an array of class objects representing interfaces, these objects are wrapped into
     *              resource objects and initialized under the specified permanent privilege
     * @param serv  the server that this permanent privilege exists
     */
    public static PermanentPrivilege initialize(PermanentPrivilege group, Class[] cls, Server serv){
        // we only intialize interfaces
        for(int x=0; x<cls.length; x++){
            if(cls[x].isInterface()){
                Resource res=new Resource(cls[x]);
               group.addResource(res);
            }
            else{
              throw new IllegalArgumentException(""+cls+" is not an interface");  
            }
        }
        saveResources(group, serv);
        return group;
    }
    
    /**
     * This method changes the name of a permanent privilege
     * @param newName the new name we want to call  a permanent privilege
     */
    @Override
    public void setName(String newName){
       Datastore.updateSingleEntity("RESOURCE_GROUPS","GROUP_ID",this.privilegeID,"NAME",newName,FilterOperator.EQUAL);
       privilegeCache.remove(this.getName());
       super.setName(newName);
    }
    
    /**
     * this method changes the level of this permanent privilege
     * @param level the new level we want to assign to this privilege
     */
    
    @Override
    public void setGroupLevel(Integer level){
       Datastore.updateSingleEntity("RESOURCE_GROUPS","GROUP_ID",this.privilegeID,"LEVEL",level.toString(),FilterOperator.EQUAL);
       privilegeCache.remove(this.getName());
       super.setGroupLevel(level);
    }
    
    /**
     * this method returns the system generated id of a permanent privilege
     */
    public String getPermanentPrivilegeID(){
        return this.privilegeID;
    }
    
    /**
     * 
     * @return the time when this privilege was created
     */
    public Date getCreationTime(){
        return this.created;
    }
    
    /**
     * this method gets an instance of an existing permanent privilege
     */
    private static PermanentPrivilege getExistingPermanentPrivilege(String name, Server serv) throws NonExistentPermanentPrivilegeException{   
    	Entity privilege = Datastore.getSingleEntity("RESOURCE_GROUPS", "NAME", name,FilterOperator.EQUAL);
        if(privilege == null){
          throw new NonExistentPermanentPrivilegeException();
        }
        Long level = (Long) privilege.getProperty("LEVEL");
        boolean accState = privilege.getProperty("ACCESS_STATE").equals("1") ? true : false;
        String privID = (String) privilege.getProperty("GROUP_ID");
        Date created = new Date((Long)privilege.getProperty("CREATED"));
        PermanentPrivilege pp = new PermanentPrivilege(name, level.intValue(),serv,accState, privID,created);
        serv.addToPrivilegeList(pp);
        privilegeCache.put(name,pp);
        return pp;
    }
    
    
    
    
    /**
     * this method saves the resources in a static resource group
     */
    private static void saveResources(PermanentPrivilege group, Server serv){
    	for(int x=0; x<group.size(); x++){
    		UniqueRandom ur=new UniqueRandom(7);
    		String rID = ur.nextRandom();
    		Filter filter = new FilterPredicate("GROUP_ID",FilterOperator.EQUAL,group.getPermanentPrivilegeID());
    		Filter filter1 = new FilterPredicate("NAME",FilterOperator.EQUAL,group.getMemberNames().get(x));
    		Entity en = Datastore.getSingleEntity("RESOURCES",filter,filter1);
            if(en != null)
            	return;
    		Entity resource = new Entity("RESOURCES");
    		resource.setProperty("RESOURCE_ID",rID);
    		resource.setProperty("GROUP_ID",group.getPermanentPrivilegeID());
    		resource.setProperty("NAME",group.getMemberNames().get(x));
    		resource.setProperty("CREATED", System.currentTimeMillis());
    		Datastore.insert(resource);
      }
    }
    
  
 
    
   
    
    private void savePrivilege(PermanentPrivilege priv, Action action) throws PermanentPrivilegeExistsException{
    	UniqueRandom ur=new UniqueRandom(10);
    	String privID = ur.nextRandom();
    	this.privilegeID=privID;
    	this.created = new Date();
    	Entity en = Datastore.getSingleEntity("RESOURCE_GROUPS","NAME", priv.getName(),FilterOperator.EQUAL);
    	if(en != null)
    		throw new PermanentPrivilegeExistsException();
    	
    	Entity rg = new Entity("RESOURCE_GROUPS");
    	rg.setProperty("GROUP_ID",privID);
    	rg.setProperty("NAME",priv.getName());
    	rg.setProperty("LEVEL",priv.getGroupLevel());
    	rg.setProperty("ACCESS_STATE","1");
    	rg.setProperty("CREATED",System.currentTimeMillis());
    	rg.setProperty("ACTION_ID",action.getActionID());
    	Datastore.insert(rg);
        action.saveAction();
        this.serv.addToPrivilegeList(priv);
}
    
    /**
     * this method assigns a specific privilege to a group of
     * users defined in the arraylist groups, if the users of this groups have this 
     * privilege already, the privilege is not duplicated
     * @param groups the groups to be assigned the specified permanent privilege
     * @param priv the privilege to be assigned to the user groups
     * @param serv  the server to which this permanent privilege is being assigned in
     */
   public static void assignPrivilegeToGroups(ArrayList groups, PermanentPrivilege priv, Server serv){
      //check if service exists
       //then assign this privilege to each user whose group is specified
	   ArrayList<String> userNames=new ArrayList();
	   for(int x = 0; x < groups.size(); x++){
		   Iterable<Entity> entities = Datastore.getMultipleEntities("USERS","GROUPS",(String)groups.get(x),FilterOperator.EQUAL);
		   for(Entity en : entities){
			   userNames.add(en.getProperty("USER_NAME").toString());
		   }
		   saveGroupPrivilege(userNames,serv,priv);
       }
       
   }
   
   public static void revokePrivilegeFromGroups(ArrayList groups, PermanentPrivilege priv, Server serv){
        ArrayList<String> userNames=new ArrayList();
        for(int x=0; x<groups.size(); x++){
        	Iterable<Entity> entities = Datastore.getMultipleEntities("USERS","GROUPS",(String)groups.get(x),FilterOperator.EQUAL);
        	for(Entity en : entities){
 			   userNames.add(en.getProperty("USER_NAME").toString());
 		    }
              revokePrivilege(serv, userNames, priv); 
        }
   }
   
   
   
   private static void saveGroupPrivilege(ArrayList userNames, Server serv,PermanentPrivilege priv){
       for(int x=0; x<userNames.size(); x++){
            try {
                 User user=User.getExistingUser((String)userNames.get(x));
                 user.grantPrivileges( priv);
            } catch (NonExistentUserException ex) {

            }
        }
   }
   
   private static void revokePrivilege(Server serv, ArrayList userNames, PermanentPrivilege priv){
      for(int x=0; x<userNames.size(); x++){
            try {
                User user=User.getExistingUser((String)userNames.get(x));
                user.revokePrivileges( priv);
            } catch (NonExistentUserException ex) {
            }
      }
    }
   
   /**
    * this method completely deletes a privilege and no further reference can
    * be made to that privilege. if any users had been granted that privilege
    * it is revoked
    * @param privName the name of the privilege
    * @param serv  the server where the privilege was initially created
    */
   public static void deletePrivilege(String privName, Server serv) throws NonExistentPermanentPrivilegeException{
        PermanentPrivilege privilege = PermanentPrivilege.getPrivilege(privName, serv);
        String privID=null;
        Entity en = Datastore.getSingleEntity("RESOURCE_GROUPS", "NAME", privName,FilterOperator.EQUAL);
        if(en == null)
        	return;
        Datastore.deleteMultipleEntities("RESOURCE_GROUPS", "GROUP_ID",privID,FilterOperator.EQUAL);
        Datastore.deleteMultipleEntities("RESOURCES","GROUP_ID",privID,FilterOperator.EQUAL);
        Datastore.deleteMultipleEntities("PRIVILEGES","GROUP_ID",privID,FilterOperator.EQUAL);
        serv.removeFromPrivilegeList(privilege);
            //TODO make sure when a privilege is revoked the superiority of a user is updated
        privilegeCache.remove(privName);
       
   }
   
   /**
    * empties all cached permanent privileges
    */
   public static void emptyPrivilegeCache(){
       privilegeCache.clear();
   }
}
