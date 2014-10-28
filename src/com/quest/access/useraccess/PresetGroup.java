
package com.quest.access.useraccess;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.verification.UserAction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author constant oduol
 * @version 1.0(18/6/12)
 */

/**
 * a preset group is a way of easily creating users by putting together commonly assigned privileges
 * and services under one name, after doing this users can be created by assigning them to a preset group
 * instead of manually assigning single privileges and services one at a time.
 */
public class PresetGroup {
    /**
     * this is the server this preset group belongs to
     */
    private Server serv;
    /**
     * this is a cache to store data about preset groups
     */
    private static ConcurrentHashMap<String, PresetGroup> presetCache = new ConcurrentHashMap();
    /**
     * this is the unique system generated id for a preset group
     */
    private String presetID;
    
    /*
     * the name of the preset group
     */
    private String name;
    
    /*
     * this is when this preset group was created
     */
    private Date created;
    
    /*
     * the name of the group of users associated with this preset group
     */
    private String groupName;
    
    /*
     * the privileges associated with this preset group
     */
    
    private ArrayList privs;
    /**
     * this creates a preset group object 
     * @param name the name of the preset group, this is supposed to be unique
     * @param groupName the group of users associated with this preset group
     * @param serv the server where this preset group belongs
     * @param services the services associated with this preset group
     * @param priv the privileges associated with this preset group
     */
   public PresetGroup(String name,String groupName,Server serv,ArrayList privs,UserAction action) throws PresetGroupExistsException{
     this.name=name;
     this.groupName=groupName;
     this.privs=privs;
     this.serv=serv;
     this.created=new Date();
     savePresetGroup(privs, action);
    }
    
    private PresetGroup(String name, String groupName, String presetID, ArrayList privileges,Server serv, Date created){
        this.name=name;
        this.groupName=groupName;
        this.presetID=presetID;
        this.privs=privileges;
        this.serv=serv;
        this.created=created;
    }
    /**
     * this method returns the unique system generated id for this
     * preset group
     */
    public String getPresetId(){
       return this.presetID; 
    }
    
    /**
     * this method returns the name of the preset group
     */
    
    public String getPresetGroupName(){
        return this.name;
    }
    /**
     * this method changes the name of this preset group
     * @param name the new name of the preset group
     */
    public void setPresetGroupName(String name){
        Datastore.updateSingleEntity("PRESET_GROUPS","PRESET_ID",this.presetID,"PRESET_NAME",name,FilterOperator.EQUAL);
        presetCache.remove(this.name);
        this.name=name;
    }
    /**
     * this method returns the name of the user group associated with this
     * preset group
     */
    public String getGroupName(){
        return this.groupName;
    }
    /**
     * this method changes the user group associated with this preset group
     * @param groupName the new group to be associated with this preset group
     */
    public void setGroupName(String groupName){
       Datastore.updateSingleEntity("PRESET_GROUPS","PRESET_ID",this.presetID,"GROUP_NAME",groupName,FilterOperator.EQUAL);
    }
    /**
     * this method returns permanent privileges of this preset group
     */
    public ArrayList getPermanentPrivileges(){
        return this.privs;
    }
    
    /**
     * 
     * @return the time this preset group was created
     */
    public Date getCreationTime(){
       return this.created; 
    }
    
    private void savePresetGroup(ArrayList privs, UserAction action) throws PresetGroupExistsException{
    	UniqueRandom ur=new UniqueRandom(10);
    	String pr_id=ur.nextMixedRandom();
    	this.presetID=pr_id;
    	Entity en = Datastore.getSingleEntity("PRESET_GROUPS", "PRESET_NAME",this.name,FilterOperator.EQUAL);
    	if(en != null){
    		throw new PresetGroupExistsException(); 
    	}
    	else{
             Entity preset = new Entity("PRESET_GROUPS");
             preset.setProperty("PRESET_ID", pr_id);
             preset.setProperty("PRESET_NAME",name);
             preset.setProperty("GROUP_NAME",groupName);
             preset.setProperty("CREATED",System.currentTimeMillis());
             preset.setProperty("ACTION_ID",action.getActionID());
             Datastore.insert(preset);
             action.saveAction();
             savePresetPrivileges(pr_id, privs);
         } 
    }
    
    private void savePresetPrivileges(String presetID, ArrayList privs){
          for(int x=0; x<privs.size(); x++){
            PermanentPrivilege priv=(PermanentPrivilege)privs.get(x);  
            String privID= priv.getPermanentPrivilegeID();
            Entity en = new Entity("PRESET_PRIVILEGES");
            en.setProperty("PRESET_ID",presetID);
            en.setProperty("PRIVILEGE_ID",privID);
            Datastore.insert(en);
          }
     }
    
    
    
    /**
     * this method deletes the specified preset group and all its associations of privileges and
     * services
     * @param presetGroup the name of the preset group to be deleted
     * @param serv the server this preset group was initially created
     */
    public static void deletePresetGroup(String presetGroup ,Server serv){
        Entity preset = Datastore.getSingleEntity("PRESET_GROUPS", "PRESET_NAME",presetGroup,FilterOperator.EQUAL);
        if(preset == null){
          return;
        }
        String preID = (String) preset.getProperty("PRESET_ID");
        Datastore.deleteMultipleEntities("PRESET_PRIVILEGES", "PRESET_ID", preID,FilterOperator.EQUAL);
        Datastore.deleteSingleEntity("PRESET_GROUPS", "PRESET_ID", preID,FilterOperator.EQUAL);
        presetCache.remove(presetGroup);  
    }
    
    /**
     * this method gets an instance of an existing preset group and ensures persistence between 
     * newly created preset group objects and those object obtained from the database
     * @param name the name of the existing preset group
     * @param serv the server this preset group was originally created
     * @return the specified preset group is returned if it is existent otherwise an exception is thrown
     * @throws NonExistentPresetGroupException 
     */
    
    public static PresetGroup getExistingPresetGroup(String name, Server serv) throws NonExistentPresetGroupException{
    	 if(presetCache.containsKey(name)){
    		return presetCache.get(name);
    	 }
         Entity pg = Datastore.getSingleEntity("PRESET_GROUPS", "PRESET_NAME",name,FilterOperator.EQUAL);
         String preID = (String) pg.getProperty("PRESET_ID");
         String groupName = (String) pg.getProperty("GROUP_NAME");
         Date created = new Date(Long.parseLong(pg.getProperty("CREATED").toString()));
         PresetGroup group=new PresetGroup(name, groupName, preID,new ArrayList(),serv,created);
         presetCache.put(name, group);
         return group;
    }
    
}


