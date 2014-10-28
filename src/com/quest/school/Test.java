/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.quest.access.common.datastore.Datastore;



/**
 *
 * @author connie
 */
public class Test {
    public static void main(String [] args){
       Iterable<Entity> privEntities = Datastore.getMultipleEntities("PRIVILEGES", "USER_ID", "12344",FilterOperator.EQUAL);
       Iterable<Entity> rgEntities = Datastore.getAllEntities("RESOURCE_GROUPS");
       Iterable<Entity> rsEntities = Datastore.getAllEntities("RESOURCES");
       HashMap<String,String> rsGroups = new HashMap<String,String>();
       HashMap<String,String> rgGroups = new HashMap<String,String>();
       HashMap<String, ArrayList<String>> privileges = new HashMap();
       for(Entity resource : rsEntities){
  		 String rsGroupId = (String) resource.getProperty("GROUP_ID");
  		 String rsName = (String) resource.getProperty("NAME");
  		 rsGroups.put(rsGroupId,rsName);
	   }
       for(Entity resourceGroup : rgEntities){
    	   String rgGroupId = (String) resourceGroup.getProperty("GROUP_ID");
    	   String rgName = (String) resourceGroup.getProperty("NAME");
    	   rgGroups.put(rgGroupId,rgName);
  	   }
       for(Entity privilege : privEntities){
    	   String privGroupId = (String) privilege.getProperty("GROUP_ID");
    	   boolean rsContains = rsGroups.keySet().contains(privGroupId);
    	   boolean rgContains = rgGroups.keySet().contains(privGroupId);
    	   if(rsContains && rgContains){
    		   String resourceName = rsGroups.get(privGroupId);;
               String resourceGroupName = rgGroups.get(privGroupId);
               // check that we havent stored this resource group before
                if(!privileges.containsKey(resourceGroupName)){
                    privileges.put(resourceGroupName, new ArrayList());
                    privileges.get(resourceGroupName).add(resourceName);
                }
                else{
                 // this resource group already exists   
                  privileges.get(resourceGroupName).add(resourceName);  
                }  
    	   }
       }
      
    }
}
