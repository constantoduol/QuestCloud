/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.servlets;

import com.quest.access.common.UniqueRandom;
import com.quest.access.control.Server;
import com.quest.access.useraccess.User;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author connie
 */
   
public class ClientWorker {
    private String msg;
    private String service;
    private JSONObject requestData;
    private Object responseData;
    private HttpSession session;
    private String id;
    private String rootId;
    private String reason;
    private HttpServletResponse response;
    private static UniqueRandom randomGen = new UniqueRandom(20);
    private static Server server = ServerLink.getServerInstance();
    /**
     * 
     * @param msg the message specifying the method to be invoked on the server
     * @param service the service specifies the class containing the method to be invoked on the server
     * @param requestData this is data the client is sending to the server
     * @param session this is a session object representing the current client
     * @param ctx this is the asynchronous context bound to the current request
     * @param response this is the response where the data will be sent back to the client
     */
    public ClientWorker(String msg,String service,JSONObject requestData,HttpSession session,HttpServletResponse response){
       this.msg=msg;
       this.service=service;
       this.requestData=requestData;
       this.session=session;
       this.response=response;
       this.id = randomGen.nextMixedRandom();
    }
    
    @Override
    public String toString(){
       return "Message : "+msg+", Service : "+service+" , Request_data : "+requestData;
    }
    

    public void work(){
         try{
             if(msg.equals("logout")){
                String userName=requestData.optString("user_name");
                server.doLogOut(this, userName);
                this.setResponseData("success");
                server.messageToClient(this);
             }
             else if(msg.equals("login")){
                requestData.put("clientip", InetAddress.getLocalHost().getHostAddress());
                server.doLogin(this);
             }
             else if(msg.equals("changepass")){
               String userName = requestData.optString("user_name");
               String oldPass = requestData.optString("old_password");
               String newPass = requestData.optString("new_password");
               Boolean change = User.changePassword(server,userName,oldPass, newPass);
               JSONObject object = new JSONObject();
               object.put("response", change);
               toClient(object);
             }
            else{
              if(checkTimeout()){ // dont proceed if client has been timed out
                 return;
              }
              server.invokeMultipleServices(this);
           }
         
        }
        catch(Exception e){
            try {
                JSONObject object =new JSONObject();
                object.put("exception", e);
                toClient(object);
            } catch (JSONException ex) {
                Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
      }
    }
   /**
    * 
    * @return this is the message used to know the method to be invoked on the server
    */
   public String getMessage(){
     return this.msg;   
   }
   
   /**
    * 
    * @return this is used to determine the class containing the method to be invoked
    */
   public String getService(){
     return this.service;   
   }
   
   /**
    * 
    * @return returns the data sent to the server represented by this client worker object
    */
   public JSONObject getRequestData(){
       return this.requestData;
   }
   
   public Object getResponseData(){
       return this.responseData;
   }
   
   public HttpServletResponse getResponse(){
       return this.response;
   }
   
   public HttpSession getSession(){
       return this.session;
   }
   
   
   public void setMessage(String msg){
     this.msg=msg;  
   }
   
   public void setService(String svc){
     this.service=svc;  
   }
   
   public String getID(){
       return this.id;
   }
   
   /**
    * this sets the id of the initial worker that spawned this worker
    */
   public void setRootWorkerID(String rootId){
      this.rootId = rootId;
   }
   
   /**
    * this gets the id of the initial worker that spawned this worker
    */
   public String getRootWorkerID(){
     return this.rootId;  
   }
   
   public void setReason(String reason){
      this.reason=reason; 
   }
    public String getReason(){
      return this.reason; 
   }
    
    public void setRequestData(JSONObject data){
       this.requestData=data; 
    }
    
    public void setResponseData(Object data){
        this.responseData = data;
    }
   /**
    * @param response this is a json object containing the data we are sending to the client
    * when the data is sent to the client the asynchronous context is completed
    */
   public void toClient(JSONObject resp){
	   try {
		   JSONObject toClient=new JSONObject();
		   toClient.put("response", resp);
		   toClient.put("message", msg);
		   PrintWriter writer = response.getWriter();
		   writer.println(toClient);
		 } catch (Exception ex) {
               System.out.println(ex);
       }
   }
   
 private double compareDate(long last, long current){
    long duration=current-last;
    return (double)duration/60000.0;
  }
    
 
 

  private boolean checkTimeout(){
      String clientID=session.getId();
      ConcurrentHashMap<String,Long> lastClientRequest =server.getTimeoutData();
      if(server.getClientTimeout()!=0){ // server is set to timeout clients
           boolean contains=lastClientRequest.containsKey(clientID); 
           long currentRequest=new Date().getTime();
           if(contains){  // client has made a request before 
               long lastRequest=lastClientRequest.get(clientID);
               if(compareDate(lastRequest, currentRequest)>server.getClientTimeout()){ // client has timed out
                        if(session!=null){
                           String name= (String) session.getAttribute("username");
                           boolean userLoggedIn = User.isUserLoggedIn(name, server);
                           if(userLoggedIn){
                             server.doLogOut(this,name);
                             lastClientRequest.replace(clientID, currentRequest);
                             server.resetTimeout(clientID);
                             server.resetLoginAttempts(name);
                             this.setResponseData(new Exception("User has been timed out"));
                             server.exceptionToClient(this);
                             return true;
                           }
                           else{
                        	   this.setResponseData(new Exception("User has been timed out"));
                               server.exceptionToClient(this);
                               return true;
                           }
                        }
                        return true;
               }
               lastClientRequest.replace(clientID, currentRequest);
               return false;
           }
           else{ // this is a clients first request
               lastClientRequest.put(clientID, currentRequest);
               return false;
           }
      
      }
     return false;
    
  }
  
  

}
