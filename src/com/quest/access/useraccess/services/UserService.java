
package com.quest.access.useraccess.services;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.quest.access.useraccess.services.annotations.Endpoint;
import com.quest.access.useraccess.services.annotations.WebService;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.*;
import com.quest.access.useraccess.verification.*;
import com.quest.servlets.ClientWorker;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author constant oduol
 * @version 1.0(23/6/12)
 */

/**
 * this class provides the core services for managing users on a quest access server
 * only users with the required privileges have access to this service.
 * The privilege associated with this service should only be granted to administrators who
 * are assigned to manage users in terms of creating user accounts, deleting user accounts,
 * disabling user accounts, granting and revoking privileges to and from other users etc.
 * The only person with access to this service is the root user who is granted all the privileges
 * automatically at system creation time.
 * <p>
 * <ol>
 *   <li>create_user : this is the message sent to the server in order to create a new user</li>
 *   <li>delete_user : this message is sent to the server by a client in order to delete a user</li>
 *   <li>permanent_delete_user : this message is sent to irreversibly delete a user</li>
 *   <li>reset_pass : this message is sent to reset a users password to the default </li>
 *   <li>edit_user : this message is sent to edit a users details</li>
 *   <li>disable_user: this message is sent to disable a user</li>
 *   <li>enable_user : this message is sent to enable a user</li>
 *   <li>grant_privilege : this message is sent to grant a user a given privilege</li>
 *   <li>revoke_privilege : this message is sent to revoke a privilege from a user</li>
 *   <li>undelete_user : this message is sent to request the server to undelete a user</li>
 *   <li>rename_deleted_user: this message is sent to the server to rename a deleted user in the USER_HISTORY table</li>
 *   <li>single_session : this message is sent to request for the session of a given user</li>
 *   <li>many_sessions : this message is sent to request for multiple sessions from the server</li>
 *   <li>view_user : this message is sent to the server to request for the details of a given user</li>
 *   <li>change_pass : this message is sent to the server to request for a password change</li>
 *  <li>verify_action : this message is sent to the server with an action serial in order to verify an action</li>
 *  <li>view_action : this message is sent to the server to request for the details of an unverified action</li>
 *  <li>delete_action : this message is sent to the server to request for the specified action to be deleted</li>
 * <li>last_action : this message is sent to the server to request for the last unverified action of a user</li>
 *</ol>
 * </p>
 * <p>
 * <ol>
 *   <li>create_preset_group : this message tells the server to create a new preset group</li>
 *   <li>delete_preset_group : this message tells the server to delete the specified preset group</li>
 *   <li>view_preset_group : this message tells the server to send the details of the preset group</li>
 *   <li>all_preset_names : this message is sent to retrieve the names of all preset groups</li>
 *   <li>all_user_groups : this message is sent to request for the names of all user groups</li>
 * </ol>
 * </p>
 * <p>
 * <ol>
 *    <li>action_history : this message is sent to request for a list of a user's actions</li>
 *    <li>login_history : this message is sent to request for a list of a user's login history</li>
 *    <li>logout_history : this message is sent to request for a list of a user's logout history</li>
 * </ol>
 * </p>
 * @see com.quest.access.net.Request
 * @see com.quest.access.net.Response
 */

@WebService (name = "user_service", level = 10, privileged = "yes")
public class UserService implements Serviceable{
    public static  final String SERVICE_NAME="user_service";
    
   
    
    @Endpoint(name="create_user")
     public synchronized void createUser(Server serv, ClientWorker worker){
        serv.setDebugMode(true);
        UserAction uAction;
        User user;
        JSONObject details=worker.getRequestData();
        String uName= details.optString("name");
        try{
            uAction=new UserAction(serv,worker, "CREATE_USER "+uName+"",new NumberVerification(2));   
          }
        catch(Exception e){
            // send only the pending verification exception, do not send the 
            // incomplete verification exception since the end user does not need to know about it
            //TODO it clogs up the messages sent to the end user 
        	  worker.setResponseData(e);
              serv.exceptionToClient(worker);
            return;
        }
        String host=details.optString("host");
        JSONArray priv=details.optJSONArray("privs");
        String group=details.optString("group");
        String presetGroup=details.optString("preset_group");
        PermanentPrivilege [] privs=new PermanentPrivilege[priv.length()];
        
        try{
          for(int x=0; x<privs.length; x++){
             privs[x]=PermanentPrivilege.getPrivilege( ((String)priv.get(x)).trim(), serv);
           }
        }
        catch(Exception e){
          worker.setReason(e.getMessage());
          worker.setResponseData(e);
          serv.exceptionToClient(worker);
          return;
        }
          if(presetGroup==null || "".equals(presetGroup)){
               try {
                   if("".equals(group) || group==null){
                      group="unassigned";
                   }
                user=new User(uName,"pass", host, group,uAction, privs);
              } catch (UserExistsException ex) {
                worker.setReason(ex.getMessage());
                worker.setResponseData(ex);
                serv.exceptionToClient(worker);
                return;
            }
          }
          else{
            try {
                 PresetGroup gp=PresetGroup.getExistingPresetGroup(presetGroup, serv);
                 user=new User(uName, "pass", host,uAction, gp);
            } catch (Exception ex) {
                worker.setReason(ex.getMessage());
                worker.setResponseData(ex);
                serv.exceptionToClient(worker);
                return;
            }
          }
      
            //save the user apps
            //user.setAccessibleApps(apps);
          worker.setResponseData(Message.SUCCESS);
          serv.messageToClient(worker);
      }
     
    
    @Endpoint(name="delete_user")
     public  void deleteUser(Server serv, ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String uName=requestData.optString("name");
            HttpSession ses=worker.getSession();
            String name=(String)ses.getAttribute("username");
            if(uName.equals("root") || name.equals(uName)){
               worker.setReason("you cannot delete your own account or a root account ");
               worker.setResponseData(Message.FAIL);
               serv.messageToClient(worker);
               return;  
             }
            User.deleteUser(uName);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "DELETE_USER "+uName+"");
            action.saveAction();
        } catch (Exception ex) {
        	 worker.setResponseData(ex);
             serv.exceptionToClient(worker);
        }
     }
     
    
    @Endpoint(name="permanent_delete_user")
     public  void permanentlyDeleteUser(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String uName=requestData.optString("name");
            HttpSession ses=worker.getSession();
            String name=(String)ses.getAttribute("username");
            if(uName.equals("root") || name.equals(uName)){
               worker.setResponseData(Message.FAIL);
               serv.messageToClient(worker);
               return;  
             }
            User.permanentlyDeleteUser(uName);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "PERMANENT_DELETE_USER "+uName+"");
            action.saveAction();
        } catch (Exception ex) {
        	  worker.setResponseData(ex);
              serv.exceptionToClient(worker);
        }
     }
    
    
  
     
  
     
    @Endpoint(name="edit_host")
     public void editHost(Server serv, ClientWorker worker){
        JSONObject details=worker.getRequestData();
        String name=(String)details.optString("user_name");
        String host=(String)details.optString("host");
        HttpSession ses=worker.getSession();
        String uName=(String)ses.getAttribute("username");
         if(name.equals("root") || name.equals(uName)){
        	 worker.setResponseData(Message.FAIL);
             serv.messageToClient(worker);
            return;  
          }
        try {
            User user = User.getExistingUser(name);
            user.setHostName(host);
            User.removeFromUserCache(name);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "EDIT_HOST "+name+"");
         action.saveAction();
        } catch (Exception ex) {
        	worker.setResponseData(ex);
            serv.exceptionToClient(worker);
        }
     }
     
     @Endpoint(name="edit_group")
      public void editGroup(Server serv,ClientWorker worker){
        JSONObject details=worker.getRequestData();
        String name=(String)details.optString("user_name");
        String group=(String)details.optString("group");
        HttpSession ses=worker.getSession();
        String uName=(String)ses.getAttribute("username");
         if(name.equals("root") || name.equals(uName)){
        	 worker.setResponseData(Message.FAIL);
             serv.messageToClient(worker);
            return;  
          }
        try {
            User user = User.getExistingUser(name);
            user.setUserGroup(group);
            User.removeFromUserCache(name);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "EDIT_GROUP "+name+"");
            action.saveAction();
        } catch (Exception ex) {
        	 worker.setResponseData(ex);
             serv.exceptionToClient(worker);
        }
     }
     
     @Endpoint(name="edit_user")
     public synchronized  void editUser(Server serv,ClientWorker worker){
        JSONObject details=worker.getRequestData();
        String name=details.optString("user_name");
        HttpSession ses=worker.getSession();
        String uName=(String)ses.getAttribute("username");
        if(name.equals("root") || name.equals(uName)){
            worker.setReason("you cannot edit a root user account or your own account");
            worker.setResponseData(Message.FAIL);
            serv.messageToClient(worker);
            return;  
          }
        String host=details.optString("host"); 
        String group=details.optString("group"); 
        JSONArray privs=(JSONArray)details.opt("privs");
        ArrayList userPrivs=new ArrayList();
          
        try {
           User user=User.getExistingUser(name);
           HashMap userPrivileges = user.getUserPrivileges();
           userPrivs.addAll(userPrivileges.keySet());
           PermanentPrivilege[] grantPrivs=new PermanentPrivilege[privs.length()];
           PermanentPrivilege[] revokePrivs=new PermanentPrivilege[userPrivs.size()];
      
             for(int x=0; x<privs.length(); x++){
               grantPrivs[x]=PermanentPrivilege.getPrivilege(((String)privs.get(x) ).trim(), serv);   
             }
             for(int x=0; x<userPrivs.size(); x++){
               revokePrivs[x]=PermanentPrivilege.getPrivilege(((String)userPrivs.get(x)).trim(), serv);   
             }
            user.setHostName(host);
            user.setUserGroup(group);
            user.revokePrivileges(revokePrivs);
            user.grantPrivileges(grantPrivs);
            User.removeFromUserCache(name);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "EDIT_USER "+name+"");
            action.saveAction();
        } catch (Exception ex) { 
        	 worker.setResponseData(ex);
             serv.exceptionToClient(worker);
        }
     }
     
   @Endpoint(name="reset_pass")
   public  void resetPassword(Server serv,ClientWorker worker){
      JSONObject requestData= worker.getRequestData();
      String uName=requestData.optString("name");
        try {
            HttpSession ses=worker.getSession();
            String name=(String)ses.getAttribute("username");
           if(uName.equals("root") || name.equals(uName)){
            worker.setReason("you cannot reset your own password");
            worker.setResponseData(Message.FAIL);
            serv.messageToClient(worker);
            return;
           }
            User user = User.getExistingUser(uName);
            user.setPassWord("pass");
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            UserAction action=new UserAction(serv,worker, "RESET_PASS "+uName+"");
            action.saveAction();
        } catch (Exception ex) {
        	worker.setResponseData(ex);
            serv.exceptionToClient(worker);
        }
     }
     
   
   @Endpoint(name="disable_user")
   public  void disableUser(Server serv,ClientWorker worker){
      try {
          JSONObject requestData= worker.getRequestData();
          String uName=requestData.optString("name");
          HttpSession ses=worker.getSession();
          String name=(String)ses.getAttribute("username");
          if(uName.equals("root") || name.equals(uName)){
             worker.setResponseData(Message.FAIL);
             serv.messageToClient(worker);
             return;
           }
          User.disableUser(true, uName);
          worker.setResponseData(Message.SUCCESS);
          serv.messageToClient(worker);
          UserAction action=new UserAction(serv,worker, "DISABLE_USER "+uName+"");
          action.saveAction();
      } catch (Exception ex) {
         worker.setResponseData(ex);
         serv.exceptionToClient(worker);
      }
   }
     
    
   @Endpoint(name="enable_user")
   public  void enableUser(Server serv, ClientWorker worker){
      try {
          JSONObject requestData= worker.getRequestData();
          String uName=requestData.optString("name");
          User.disableUser(false, uName);
          worker.setResponseData(Message.SUCCESS);
          serv.messageToClient(worker);
          UserAction action=new UserAction(serv,worker, "ENABLE_USER "+uName+"");
          action.saveAction();
      } catch (Exception ex) {
         worker.setResponseData(ex);
         serv.exceptionToClient(worker);
      }
   }
     
   @Endpoint(name="grant_privilege")
   public  void grantPrivilege(Server serv,ClientWorker worker){
       JSONObject details=worker.getRequestData();
       try{
          String priv =details.optString("priv");
          String uName=details.optString("name");
          HttpSession ses=worker.getSession();
          String name=(String)ses.getAttribute("username");
         if(name.equals(uName)){
          worker.setReason("you cannot grant yourself privileges");
          worker.setResponseData(Message.FAIL);
          serv.messageToClient(worker);
          return;
         }
       PermanentPrivilege [] privileges=new PermanentPrivilege[]{PermanentPrivilege.getPrivilege(priv, serv)};
       User user=User.getExistingUser(uName);
       user.grantPrivileges(privileges);
       worker.setResponseData(Message.SUCCESS);
       serv.messageToClient(worker);
       }
       catch(Exception e){ 
          worker.setResponseData(e);
          serv.exceptionToClient(worker);
       }
   }
     
    
   @Endpoint(name="revoke_privilege")
   public  void revokePrivilege(Server serv, ClientWorker worker){
       JSONObject details=worker.getRequestData();
       try{
          String priv =details.optString("priv");
          String uName=details.optString("name");
          HttpSession ses=worker.getSession();
          String name=(String)ses.getAttribute("username");
          if(name.equals(uName)){
              worker.setReason("you cannot revoke your own privileges");
              worker.setResponseData(Message.FAIL);
              serv.messageToClient(worker);
              return;
         }
        SecurityException sExp=new SecurityException("group root cannot have specified privileges revoked");
        if((priv.equals("user_service") || priv.equals("privilege_service")) && uName.equals("root") ){
           worker.setResponseData(sExp);
           serv.exceptionToClient(worker);
        }
       PermanentPrivilege [] privileges=new PermanentPrivilege[]{PermanentPrivilege.getPrivilege(priv, serv)}; 
       User user=User.getExistingUser(uName);
       user.revokePrivileges(privileges);
       worker.setResponseData(Message.SUCCESS);
       serv.messageToClient(worker);
       }
       catch(Exception e){
        worker.setResponseData(e);
        serv.exceptionToClient(worker);
       }
       
   }
     
    
   @Endpoint(name="create_preset_group")
   public  void createPresetGroup(Server serv, ClientWorker worker){
     JSONObject details=worker.getRequestData();
     String name=details.optString("name");
     String group=details.optString("group");
     JSONArray privs =(JSONArray) details.opt("privs");
     ArrayList privileges=new ArrayList();
     try {
     for(int x=0; x<privs.length(); x++){
        PermanentPrivilege privilege = PermanentPrivilege.getPrivilege(privs.getString(x).trim(), serv);
        privileges.add(privilege);
      }
       UserAction action=new UserAction(serv,worker, "CREATE_PRESET_GROUP "+name+"");
       PresetGroup pg=new PresetGroup(name, group, serv, privileges,action);
       worker.setResponseData(Message.SUCCESS);
       serv.messageToClient(worker);
      } catch (Exception ex) {
           worker.setResponseData(ex);
           serv.exceptionToClient(worker);
      }
   }
     
     
   @Endpoint(name="delete_preset_group")
   public  void deletePresetGroup(Server serv,ClientWorker worker){
      try {
          JSONObject requestData = worker.getRequestData();
          String name=requestData.optString("name");
          PresetGroup.deletePresetGroup(name, serv);
          worker.setResponseData(Message.SUCCESS);
          serv.messageToClient(worker);
          UserAction action=new UserAction(serv,worker, "DELETE_PRESET_GROUP "+name+"");
          action.saveAction();
      } catch (Exception ex) {
         worker.setResponseData(ex);
         serv.exceptionToClient(worker);
      }
   }
     
     
   @Endpoint(name="view_preset_group")
   public  void viewPresetGroup(Server serv, ClientWorker worker){
      JSONObject requestData = worker.getRequestData();
      String name=requestData.optString("name");
      try {
         PresetGroup preGroup=PresetGroup.getExistingPresetGroup(name, serv);
         String group=preGroup.getGroupName();
         String id=preGroup.getPresetId();
         Date date=preGroup.getCreationTime();
         ArrayList privs=preGroup.getPermanentPrivileges();
         JSONArray arr=new JSONArray();
         for(int x=0; x<privs.size(); x++){
           arr.put(((PermanentPrivilege)privs.get(x)).getName());
         }
         JSONObject details=new JSONObject();
          try {
              details.put("group", group);
              details.put("created", date.toString());
              details.put("id", id);
              details.put("privileges", arr);
              
          } catch (JSONException ex) {

          } 
         worker.setResponseData(details);
         serv.messageToClient(worker);
         
      } catch (Exception ex) {
       worker.setResponseData(ex);
       serv.exceptionToClient(worker);
      }
   }
     
   @Endpoint(name="single_session")
   public  void getSingleSession(Server serv,ClientWorker worker){
      try {
          JSONObject requestData = worker.getRequestData();
          String name=requestData.optString("name");
          ConcurrentHashMap<String,HttpSession> sessions = Server.getUserSessions();
          JSONObject details=new JSONObject();
          HttpSession ses= sessions.get(name);
          if(ses==null){
            worker.setReason("user not logged in");
            worker.setResponseData(Message.FAIL);
            serv.messageToClient(worker);  
          }
          else{
           details.put("session_id", ses.getId());
           details.put("login_id", ses.getAttribute("loginid"));
           details.put("session_start", ses.getAttribute("sessionstart"));
           details.put("client_ip", ses.getAttribute("clientip"));
           worker.setResponseData(details);
           serv.messageToClient(worker);
          }
      } catch (JSONException ex) {
          java.util.logging.Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
      }
     
         
   }
     
      
     
    
      
     
     @Endpoint(name="view_user")
     public  void getUserDetails(Server serv, ClientWorker worker){
         JSONObject requestData = worker.getRequestData();
         String userName = requestData.optString("name");
         HashMap details = new HashMap();
         Entity en = Datastore.getSingleEntity("USERS", "USER_NAME",userName,FilterOperator.EQUAL);
         String userID = (String) en.getProperty("USER_ID");  
         String host = (String) en.getProperty("HOST"); 
         Double sup = (Double) en.getProperty("SUPERIORITY");
         Date created = new Date(Long.parseLong(en.getProperty("CREATED").toString()));
         String group = (String) en.getProperty("GROUPS");
         String loginID = (String) en.getProperty("LAST_LOGIN");
         try {
            User user= User.getExistingUser(userName);
            HashMap privs = user.getUserPrivileges();
            Set keySet = privs.keySet();
            Iterator iter=keySet.iterator();
            PermanentPrivilege [] priv=new PermanentPrivilege[keySet.size()];
            
            for(int x=0;iter.hasNext();x++){
              String privName=(String) iter.next();
              priv[x]=PermanentPrivilege.getPrivilege(privName, serv);
            }
            
            details.put("user_id", userID);
            details.put("host",host);
            details.put("superiority", sup);
            details.put("privileges", priv);
            details.put("group",group);
            details.put("created",created);
            details.put("last_login", "");
            worker.setResponseData(details);
            serv.messageToClient(worker);
           } catch (Exception ex) {
        	   worker.setResponseData(ex);
               serv.exceptionToClient(worker);
        }
     }
     
     
     @Endpoint(name="all_preset_names")
     public  void getAllPresetNames(Server serv,ClientWorker worker){
    	 Iterable<Entity> entities = Datastore.getAllEntities("PRESET_GROUPS");
    	 JSONArray values=new JSONArray();
    	 for(Entity en : entities){
    		 values.put(en.getProperty("PRESET_NAME"));
    	 }
    	 worker.setResponseData(values);
         serv.messageToClient(worker);
     }
     
     
     @Endpoint(name="all_user_groups")
     public  void getAllUserGroups(Server serv,ClientWorker worker){
    	 Iterable<Entity> entities = Datastore.getAllEntities("USERS");
    	 JSONArray values = new JSONArray();
    	 for(Entity en : entities){
    		 String val = en.getProperty("GROUPS").toString();
             if(!"root".equals(val) || !values.toList().contains(val)){
               values.put(val);  
             }
    	 }
    	 worker.setResponseData(values);
         serv.messageToClient(worker);
     }
     
     
     @Endpoint(name="all_privilege_names")
     public  void getAllPrivilegeNames(Server serv,ClientWorker worker){
        Iterable<Entity> entities = Datastore.getAllEntities("RESOURCE_GROUPS");
        JSONArray values=new JSONArray();
        for(Entity en : entities){
        	 values.put(en.getProperty("NAME"));
        }
        worker.setResponseData(values);
        serv.messageToClient(worker);
    }
     
    @Endpoint(name="edit_screen_details")
    public  void populateEditScreen(Server serv,ClientWorker worker){
        JSONObject requestData = worker.getRequestData();
        String name=requestData.optString("name");
        try {
            User user = User.getExistingUser(name);
            String host=user.getHostName();
            String group=user.getUserGroup();
            String id=user.getSystemUserId();
            Date created=user.getCreationTime();
            double sup=user.getUserSuperiority();
            boolean disabled=User.isUserDisabled(name, serv);
            boolean loggedin= User.isUserLoggedIn(name, serv);
            HashMap userPrivileges =user.getUserPrivileges();
            JSONArray values=new JSONArray();
            ArrayList list=new ArrayList();
            list.addAll(userPrivileges.keySet());
            
             for(int x=0; x<list.size(); x++){
               values.put(list.get(x));  
             }
             //get user apps
            JSONObject objc=new JSONObject();
            try{
               objc.put("host", host);
               objc.put("group",group);
               objc.put("privileges", values);
               objc.put("id",id);
               objc.put("sup",sup);
               objc.put("disabled",disabled);
               objc.put("loggedin",loggedin);
               objc.put("created",created);
            }
            catch(Exception e){
              
            }
            worker.setResponseData(objc);
            serv.messageToClient(worker);
          
        } catch (Exception ex) {
            worker.setReason("The specified user does not exist");
            worker.setResponseData(ex);
            serv.exceptionToClient(worker);
        }
    
    
    }
    
    @Endpoint(name="action_history")
    public  void actionHistory(Server serv,ClientWorker worker){
       JSONObject details=worker.getRequestData();
       String name=details.optString("name");
       int limit=details.optInt("limit");
       ArrayList actionHistory = User.getActionHistory(name, limit);
       JSONArray history=new JSONArray();
       for(int x=0; x<actionHistory.size(); x++){
            HashMap map=(HashMap) actionHistory.get(x);
            JSONArray objc=new JSONArray();
                 objc.put(map.get("USER_ID"));
                 objc.put(map.get("ACTION_ID"));
                 objc.put(map.get("ACTION_TIME"));
                 objc.put(map.get("ACTION_DESCRIPTION"));
                 history.put(objc);
            
         }
       worker.setResponseData(history);
       serv.messageToClient(worker);
    }
    
    
    
    @Endpoint(name="login_history")
     public  void loginHistory(Server serv,ClientWorker worker){
       JSONObject details=worker.getRequestData();
       String name=details.optString("name");
       int limit=details.optInt("limit");
       ArrayList loginHistory = User.getLoginLog(name, limit);
       JSONArray history=new JSONArray();
       for(int x=0; x<loginHistory.size(); x++){
            HashMap map=(HashMap)loginHistory.get(x);
            JSONArray objc=new JSONArray();
                 objc.put(map.get("SERVER_IP"));
                 objc.put( map.get("CLIENT_IP"));
                 objc.put( map.get("LOGIN_ID"));
                 objc.put( map.get("LOGIN_TIME"));
                 history.put(objc);
            
         }
       worker.setResponseData(history);
       serv.messageToClient(worker);
    }
    
  @Endpoint(name="logout_user")
 public void logoutUser(Server serv,ClientWorker worker){
        try {
            JSONObject details=worker.getRequestData();
            String name=details.optString("name");
            HttpSession currSes=worker.getSession();
            String currUser=(String) currSes.getAttribute("username");
            if(name.equals(currUser)){
              worker.setReason("you cannot remotely logout your own account");
              worker.setResponseData(Message.FAIL);
              serv.messageToClient(worker);   
              return;
            }
            JSONObject logData=new JSONObject();
            logData.put("user_name", name);
            ConcurrentHashMap<String,HttpSession> sessions = Server.getUserSessions();
            HttpSession ses=sessions.get(name);
            ClientWorker work = new ClientWorker("logout","",logData,ses,null);
            serv.doLogOut(work, name);
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }
 }
     
 
 @Endpoint(name="logout_history")
 public  void logoutHistory(Server serv,ClientWorker worker){
      JSONObject details=worker.getRequestData();
      String name=details.optString("name");
      int limit=details.optInt("limit");
      ArrayList logoutHistory = User.getLogoutLog(name, limit);
      JSONArray history=new JSONArray();
      for(int x=0; x<logoutHistory.size(); x++){
            HashMap map=(HashMap)logoutHistory.get(x);
            JSONArray objc=new JSONArray();
            objc.put(map.get("SERVER_IP"));
            objc.put(map.get("CLIENT_IP"));
            objc.put(map.get("LOGOUT_ID"));
            objc.put(map.get("LOGOUT_TIME"));
            history.put(objc);
         }
      worker.setResponseData(history);
      serv.messageToClient(worker);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

  @Endpoint(name="verify_action")
  public  void verifyAction(Server serv,ClientWorker worker) throws PendingVerificationException, IncompleteVerificationException, NonExistentSerialException {
        //do not verify this action if it is the same user trying to verify
       JSONObject object=worker.getRequestData();
       String serial=object.optString("serial");
       HttpSession ses=worker.getSession();
       String name= (String) ses.getAttribute("username");
       String userName= (String) UserAction.getUserNames().get(serial);
       if(userName==null){
         worker.setReason("Serial entered does not exist");
         worker.setResponseData(Message.FAIL);
         serv.messageToClient(worker);
         return;
       }
       else if(userName.equals(name)){
         //person is trying to verify his own action  
           worker.setReason("Verifying own action not allowed");
           worker.setResponseData(Message.FAIL);
           serv.messageToClient(worker);
           return;
       }
 
       UserAction.verifyAction(serial,worker,serv);
    }
     
  @Endpoint(name="view_action")
   public  void viewAction(Server serv,ClientWorker worker)  {
       JSONObject object=worker.getRequestData();
       String serial=object.optString("serial");
       ConcurrentHashMap userActions = UserAction.getUserActions();
       String userName= (String) UserAction.getUserNames().get(serial);
       if(userName==null){
         worker.setReason("Action Serial entered does not exist");
         worker.setResponseData(Message.FAIL);
         serv.messageToClient(worker);
         return;
       }
       Object[] action = (Object[])userActions.get(userName);
       JSONObject objc=new JSONObject();
        try {
            ClientWorker work=(ClientWorker) action[0];
            objc.put("service",work.getService());
            objc.put("param",work.getRequestData());
            objc.put("count",action[2]);
            objc.put("serial",action[4]);
            objc.put("username",userName);
            objc.put("message",work.getMessage());
            objc.put("time",action[3].toString());
            worker.setResponseData(objc);
            serv.messageToClient(worker);
        } catch (JSONException ex) {
      
        }
  
    }
   
  @Endpoint(name="delete_action")
   public  void deleteAction(Server serv,ClientWorker worker){
       JSONObject object=worker.getRequestData();
       String serial=object.optString("serial");   
       String userName= (String) UserAction.getUserNames().get(serial);
       HttpSession ses=worker.getSession();
       String name = (String) ses.getAttribute("username");
       if(userName==null){
          worker.setReason("Action Serial entered does not exist");
          worker.setResponseData(Message.ERROR);
          serv.messageToClient(worker);
          return;
       }
       else if(!name.equals(userName)){
         //you can only delete actions that you initiated

          worker.setReason("You can only delete actions that you initiated");
          worker.setResponseData(Message.ERROR);
          serv.messageToClient(worker);
           return;
       }
      UserAction.getUserNames().remove(serial);
      UserAction.getUserActions().remove(userName);
      worker.setResponseData(Message.SUCCESS);
      serv.messageToClient(worker);
   }

  
 @Endpoint(name="last_action")
 public  void lastAction(Server serv,ClientWorker worker){
       JSONObject object=worker.getRequestData();
       String name=object.optString("name"); 
       Object[] action = (Object[]) UserAction.getUserActions().get(name);
       if(action==null){
          worker.setReason("No pending actions for user");
          worker.setResponseData(Message.FAIL);
          serv.messageToClient(worker);
          return;  
       }
       
       JSONObject objc=new JSONObject();
        try {
            ClientWorker work=(ClientWorker) action[0];
            objc.put("service",work.getService());
            objc.put("param",work.getRequestData());
            objc.put("count",action[2]);
            objc.put("serial",action[4]);
            objc.put("message",work.getMessage());
            objc.put("time",action[3].toString());
            worker.setResponseData(objc);
            serv.messageToClient(worker);
        } catch (JSONException ex) {
      
        }
 }
 
 @Endpoint(name="all_actions")
 public  void allActions(Server serv,ClientWorker worker){
       JSONObject object=worker.getRequestData();
       int limit = object.optInt("limit");
       ConcurrentHashMap actions = UserAction.getUserActions();
       Iterator iter = actions.values().iterator();
       JSONArray all=new JSONArray();
       if(limit==0){
         limit=actions.size(); 
       }
        int count=0;
         while(iter.hasNext() && count<=limit){
           count++;
           Object[] action=(Object[])iter.next();
           ClientWorker work=(ClientWorker)action[0];
           System.out.println(action[0]);
           JSONArray details=new JSONArray();
           details.put(work.getService()); //service
           details.put(work.getRequestData().toString()); //param
           details.put(action[2]); //count
           details.put(action[4]); //serial
           details.put(UserAction.getUserNames().get(action[4])); //name
           details.put(work.getMessage()); //message
           details.put(action[3].toString()); //time
           all.put(details);
         } 
         worker.setResponseData(all);
         serv.messageToClient(worker);
 }

 

 
 
    @Override
    public void service() {
     
    }
     
     
    
}
