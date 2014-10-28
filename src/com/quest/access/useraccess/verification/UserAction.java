package com.quest.access.useraccess.verification;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.servlets.ClientWorker;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;

/**
 *
 * @author constant oduol
 * @version 1.0(23/6/12)
 */

/**
 * This class tries to store information concerning actions of users in the system
 * this is done using a verification process where users with the relevant privileges
 * or relevant authority can carry out actions in the system.
 * @author Conny
 */
  public class UserAction implements Action{  
      private String actionID;
      private Server serv;
      private String description;
      private static ConcurrentHashMap actions = new ConcurrentHashMap();
      private static ConcurrentHashMap userNames = new ConcurrentHashMap();
      private HttpSession clientSession;
      private static final String[] excludeNames=new String[]{"root"}; // this marks people who need no verification
      static{
          Arrays.sort(excludeNames);
      }
      /**
       * Constructs a user action object that describes what a user has done
       * the action is not saved until the instance method saveAction is called
       * @param serv the server in which this action was performed
       * @param clientID the id of the client who performed this action
       * @param description a small statement describing the action
       * @param serviceName this is the name of the service the user is trying to invoke
       * @param message this is the specific message to be sent to the service
       * @param param these are the parameters the service to be invoked takes
       * @param number these are the number of users required to verify this action
       */
      
      public UserAction(Server serv,ClientWorker worker, String description, UserVerification ver) throws Exception{
    	  UniqueRandom ur=new UniqueRandom(50);
          actionID=ur.nextMixedRandom();
          this.serv=serv;
          this.description=description;
          if(worker==null){
             //not enough information to verify or save the action
             return; 
          }
          this.clientSession=worker.getSession();
          String userName=(String)clientSession.getAttribute("username"); 
          if(ver==null || Arrays.binarySearch(excludeNames,userName)>-1){
              //in case no verification is needed just return
              //in case this user is on the all rights list just return and let the action be committed
             return; 
          }
          
           /*
            * at this point we check whether this user has a pending unverified action
            * if the user has an unverified action stop at this point and inform the user
            * otherwise bind the serial to the user's name
            */
          Boolean done=(Boolean)clientSession.getAttribute("action_done"); 
          if(done!=null && done==true){
            // the action has been committed no need to repeat again so return at this point
            //unmark for future processing of actions
            clientSession.setAttribute("action_done",false);
            return;
          }
          
            if(!actions.containsKey(userName)){
                // the user has no unverified action
                UniqueRandom unique=new UniqueRandom(8);
                String serial=unique.nextRandom();
                //remember that this user has a pending action
                actions.put(userName,new Object []{worker,ver,1,new Date(),serial});
                userNames.put(serial,userName);
                //send the serial to the end user
                //serv.sendToOne(clientID, new Response(serial,"serial"));
                String originalMsg=worker.getMessage();
                worker.setMessage("serial");
                worker.setResponseData(serial);
                serv.messageToClient(worker);
                worker.setMessage(originalMsg);
                //stop propagation of the action by throwing an exception
                //purpose of this exception is to stop propagation of this action only!
                throw new IncompleteVerificationException();
            }
            else{
                //if this user has a pending action serial then he should not perform any other
                //action, so just inform him
                throw new PendingVerificationException();
            }
                
      }
      
     public UserAction(Server serv, ClientWorker worker, String description,String serialID, UserVerification [] ver){
           //yet to implement
     }
      
      
      public UserAction(Server serv, ClientWorker worker, String description) throws Exception{
           this(serv,worker,description,(UserVerification)null);
      }
      
      /**
       * 
       * @return a string representing the id of this action
       */
    @Override
      public String getActionID(){
          return this.actionID;
      }
      
      /**
       * this method is called to commit an action as performed by a specific user
       */
    @Override
      public void saveAction(){
          String userName=(String) this.clientSession.getAttribute("username");
          String userID=(String) this.clientSession.getAttribute("userid");
          Entity en = new Entity("USER_ACTIONS");
          en.setProperty("ACTION_ID",this.actionID);
          en.setProperty("USER_ID",userID);
          en.setProperty("USER_NAME",userName);
          en.setProperty("ACTION_TIME", System.currentTimeMillis());
          en.setProperty("ACTION_DESCRIPTION",this.description);
          Datastore.insert(en);
      }
      
      /**
       * this method is called to get details concerning a specific action is string
       */
      public static HashMap getActionDetails(String actionId,Server serv){
    	  Iterable<Entity> entities = Datastore.getMultipleEntities("USER_ACTIONS", "ACTION_ID", actionId,FilterOperator.EQUAL);
    	  HashMap details=new HashMap();
    	  for(Entity en : entities){
    		  details.put("USER_ID",en.getProperty("USER_ID"));
              details.put("ACTION_TIME",en.getProperty("ACTION_TIME"));
              details.put("USER_NAME",en.getProperty("USER_NAME"));
              details.put("ACTION_DESCRIPTION",en.getProperty("ACTION_DESCRIPTION")); 
    	  }
          return details;
      }
      
      public static void verifyAction(String serialID,ClientWorker worker,Server serv) throws PendingVerificationException, IncompleteVerificationException, NonExistentSerialException{
        //get the stored user verification object
          String userName = (String) userNames.get(serialID);
          Object[] obj = (Object[])actions.get(userName);
          if(obj!=null){
              UserVerification ver=(UserVerification) obj[1];
              ClientWorker work=(ClientWorker) obj[0];
              worker.setRequestData(work.getRequestData()); //take the request data from the first request
              worker.setMessage(work.getMessage());
              ver.verify(worker,serialID,serv);   
            }
      }
      
      /**
       * 
       * @return the currently unverified actions
       */
     public static ConcurrentHashMap getUserActions(){
         return actions;
     }
     
     
      /**
       * 
       * @return usernames of users with pending actions
       */
     public static ConcurrentHashMap getUserNames(){
         return userNames;
     }
      
      
      
      
      
}
