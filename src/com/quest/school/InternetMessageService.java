/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quest.school;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.quest.access.common.UniqueRandom;
import com.quest.access.common.datastore.Datastore;
import com.quest.access.control.Server;
import com.quest.access.useraccess.Serviceable;
import com.quest.access.useraccess.services.Message;
import com.quest.access.useraccess.services.annotations.Endpoint;
import com.quest.access.useraccess.services.annotations.WebService;
import com.quest.access.useraccess.verification.UserAction;
import com.quest.servlets.ClientWorker;


/**
 *
 * @author connie
 */
@WebService (name = "message_service", level = 10, privileged = "yes")
public class InternetMessageService implements Serviceable {
    
    private static ConcurrentHashMap<String,ArrayList<String>> failedRecipients = new ConcurrentHashMap();
    
    private static ConcurrentHashMap<String,ArrayList<String>> failedMessages = new ConcurrentHashMap();
    
    private static ConcurrentHashMap<String,ArrayList<String>> failedRecipientNames = new ConcurrentHashMap();
    
    private static ConcurrentHashMap<String, Integer> messageSent = new ConcurrentHashMap();
    
    private static volatile ConcurrentHashMap<String, Boolean> stopSendingMessages = new ConcurrentHashMap();
    
    @Override
    public void service() {
      //dummy method
    }
    
  

    @Override
    public void onCreate() {
          onStart();
    }
    
    
    
    @Endpoint(name="create_message_list")
    public void createMessageList(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray names = requestData.optJSONArray("names");
            JSONArray phones=requestData.optJSONArray("phones");
            String listName=requestData.optString("list_name");
            UserAction action=new UserAction(serv, worker, "CREATE MESSAGE LIST");
            Entity en = Datastore.getSingleEntity("SMS_LISTS_META_DATA", "LIST_NAME", listName, FilterOperator.EQUAL);
            if(en != null){
               worker.setReason("List already exists");
               worker.setResponseData(Message.FAIL);
               serv.messageToClient(worker);
               return;
            }
            UniqueRandom rand = new UniqueRandom(10);
            String listId = rand.nextMixedRandom();
            Entity smsListMeta = new Entity("SMS_LISTS_META_DATA");
            smsListMeta.setProperty("ID",listId);
            smsListMeta.setProperty("LIST_NAME",listName);
            smsListMeta.setProperty("CREATED",System.currentTimeMillis());
            Datastore.insert(smsListMeta);
            for(int x=0; x<names.length(); x++){
            	Entity smsList = new Entity("SMS_LISTS");
            	smsList.setProperty("ID",rand.nextMixedRandom());
            	smsList.setProperty("LIST_ID",listId);
            	smsList.setProperty("PERSON_NAME",names.optString(x));
            	smsList.setProperty("PERSON_PHONE",phones.optString(x));
            	smsList.setProperty("CREATED",System.currentTimeMillis());
            	Datastore.insert(smsList);
            }
            action.saveAction();
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="all_message_lists")
    public void allMessageLists(Server serv,ClientWorker worker){
    	Iterable<Entity> entities = Datastore.getMultipleEntities("SMS_LISTS_META_DATA", "LIST_NAME", SortDirection.ASCENDING);
        JSONObject data = Datastore.entityToJSON(entities);
        worker.setResponseData(data);
        serv.messageToClient(worker);
    }
    
    @Endpoint(name="delete_message_list")
    public void deleteMessageList(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String id = requestData.optString("id");
            UserAction action=new UserAction(serv, worker, "DELETE MESSAGE LIST");
            Datastore.deleteMultipleEntities("SMS_LISTS_META_DATA", "ID",id,FilterOperator.EQUAL);
            Datastore.deleteMultipleEntities("SMS_LISTS", "ID",id,FilterOperator.EQUAL);
            action.saveAction();
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="all_list_members")
    public void allListMembers(Server serv,ClientWorker worker){
      JSONObject requestData = worker.getRequestData();
      String listId = requestData.optString("id"); 
      Filter filter = new FilterPredicate("LIST_ID",FilterOperator.EQUAL,listId);
      Iterable<Entity> entities = Datastore.getMultipleEntities("SMS_LISTS","PERSON_NAME",SortDirection.ASCENDING,filter);
      JSONObject data = Datastore.entityToJSON(entities);
      worker.setResponseData(data);
      serv.messageToClient(worker);
    }
    
    @Endpoint(name="edit_message_list")
    public void editMessageList(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray names = requestData.optJSONArray("names");
            JSONArray phones=requestData.optJSONArray("phones");
            String listName=requestData.optString("list_name");
            String oldListName=requestData.optString("old_list_name");
            String listId=requestData.optString("id");
            UserAction action=new UserAction(serv, worker, "EDIT MESSAGE LIST");
            Entity en = Datastore.getSingleEntity("SMS_LISTS_META_DATA", "LIST_NAME",listName,FilterOperator.EQUAL);
            if(!listName.equals(oldListName) && en != null){
               worker.setReason("List already exists");
               worker.setResponseData(Message.FAIL);
               serv.messageToClient(worker);
               return;
            }
            Datastore.deleteMultipleEntities("SMS_LISTS", "LIST_ID",listId,FilterOperator.EQUAL);
            UniqueRandom rand=new UniqueRandom(10);
            for(int x=0; x<names.length(); x++){
            	Entity smsList = new Entity("SMS_LISTS");
            	smsList.setProperty("ID", rand.nextMixedRandom());
            	smsList.setProperty("LIST_ID", listId);
            	smsList.setProperty("PERSON_NAME",names.optString(x));
            	smsList.setProperty("PERSON_PHONE",phones.optString(x));
            	smsList.setProperty("CREATED",System.currentTimeMillis());
            	Datastore.insert(smsList);
             }
            Datastore.updateSingleEntity("SMS_LISTS_META_DATA","ID",listId, "LIST_NAME",listName,FilterOperator.EQUAL);
            action.saveAction(); 
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Endpoint(name="all_recipients")
    public void allRecipients(Server serv,ClientWorker worker){
       JSONObject requestData = worker.getRequestData();
       String fieldValue=requestData.optString("field_value");
       Filter filter = new FilterPredicate("LIST_ID", FilterOperator.EQUAL,fieldValue);
       Iterable<Entity> entities = Datastore.getMultipleEntities("SMS_LISTS", "PERSON_NAME", SortDirection.ASCENDING, filter);
       JSONObject data = Datastore.entityToJSON(entities);
       worker.setResponseData(data);
       serv.messageToClient(worker);	
    }
    
    @Endpoint(name="create_template")
    public void createTemplate(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String tempName=requestData.optString("template_name");
            String tempValue=requestData.optString("template_value");
            Entity en = Datastore.getSingleEntity("TEMPLATE_MESSAGES", "TEMPLATE_NAME",tempName,FilterOperator.EQUAL);
            if(en != null){
               worker.setReason("The specified template already exists");
               worker.setResponseData(Message.FAIL);
               serv.messageToClient(worker);
               return;
            }
            String userId = (String) worker.getSession().getAttribute("userid");
            UniqueRandom rand=new UniqueRandom(10);
            Entity template = new Entity("TEMPLATE_MESSAGES");
            template.setProperty("ID",rand.nextMixedRandom());
            template.setProperty("TEMPLATE_NAME",tempName);
            template.setProperty("TEMPLATE_VALUE",tempValue);
            template.setProperty("TEMPLATE_OWNER",userId);
            template.setProperty("CREATED",System.currentTimeMillis());
            Datastore.insert(template);
            UserAction action=new UserAction(serv, worker, "MESSAGE_SERVICE : CREATE TEMPLATE :  "+tempName);
            action.saveAction();
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Endpoint(name="fetch_template_names")
    public void fetchTemplateNames(Server serv,ClientWorker worker){
       String userId=(String) worker.getSession().getAttribute("userid");
       Iterable<Entity> entities = Datastore.getMultipleEntities("TEMPLATE_MESSAGES","TEMPLATE_OWNER",userId,FilterOperator.EQUAL);
       JSONObject data = Datastore.entityToJSON(entities);
       worker.setResponseData(data);
       serv.messageToClient(worker);
    }
   
    @Endpoint(name="fetch_template")
    public void fetchTemplate(Server serv,ClientWorker worker){
       JSONObject requestData = worker.getRequestData();
       String id = requestData.optString("template_id");
       Iterable<Entity> entities = Datastore.getMultipleEntities("TEMPLATE_MESSAGES","ID",id,FilterOperator.EQUAL);
       JSONObject data = Datastore.entityToJSON(entities);
       worker.setResponseData(data);
       serv.messageToClient(worker);
    }
    
    @Endpoint(name="delete_template")
    public void deleteTemplate(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String id = requestData.optString("template_id");
            UserAction action=new UserAction(serv, worker, "MESSAGE_SERVICE : DELETE TEMPLATE "+id);
            Datastore.deleteSingleEntity("TEMPLATE_MESSAGES","ID",id,FilterOperator.EQUAL);
            action.saveAction();
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Endpoint(name="update_template")
    public void updateTemplate(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String id = requestData.optString("template_id");
            String name = requestData.optString("template_name");
            String value=requestData.optString("template_value");
            Datastore.updateSingleEntity("TEMPLATE_MESSAGES","ID",id,new String[]{"TEMPLATE_NAME","TEMPLATE_VALUE"},new String[]{name,value},FilterOperator.EQUAL);
            UserAction action=new UserAction(serv, worker, "MESSAGE_SERVICE : UPDATE TEMPLATE :  "+name);
            action.saveAction();
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void onStart() {
  
    }
    
 
   
   
  @Endpoint(name="target_reach")
  public void getTargetReach(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            JSONArray listIds = requestData.optJSONArray("list_ids");
            JSONObject count = new JSONObject();
            Integer finalCount = 0;
            //add the count due to the lists
            for(int x=0; x<listIds.length(); x++){
            	Iterable<Entity> entities = Datastore.getMultipleEntities("SMS_LISTS","LIST_ID",listIds.optString(x),FilterOperator.EQUAL);
                Iterator iter = entities.iterator();
                int listCount = 0 ;
            	while(iter.hasNext()){
            		iter.next();
                	listCount++;
            	}
                finalCount = finalCount+listCount;
            }
            count.put("TARGET_REACH", finalCount);
            worker.setResponseData(count);
            serv.messageToClient(worker);
        } catch (JSONException ex) {
        	Server.log(ex,Level.SEVERE,InternetMessageService.class);
        }
  }
  
  @Endpoint(name="preview_message")
  public void previewMessage(Server serv,ClientWorker worker){
        try {
            JSONObject requestData = worker.getRequestData();
            String msg=requestData.optString("msg");  
            String finalMessage= msg;
            int count=((int)Math.ceil(finalMessage.length()/160.0) );
            JSONObject obj=new JSONObject();
            obj.put("message_preview",finalMessage);
            obj.put("count", count);
            worker.setResponseData(obj);
            serv.messageToClient(worker);
        } catch (Exception ex) {
            Server.log(ex,Level.SEVERE,InternetMessageService.class);
        }
  }
  
  @Endpoint(name="send_message")
  public void sendMessage(Server serv,ClientWorker worker){
	    JSONObject requestData = worker.getRequestData();
	    String msg = requestData.optString("msg");
	    String id = worker.getSession().getId();
	    stopSendingMessages.put(id,false);
	    messageSent.put(id,0);
	    failedMessages.put(id, new ArrayList<String>());
	    failedRecipients.put(id, new ArrayList<String>());
	    failedRecipientNames.put(id, new ArrayList<String>());
	    Object [] tempData = parseMessage(msg, serv, worker);
	    if(tempData == null){
	       //we cannot send the message 
	    }
	    else{
	      sendFinalMessage(serv,worker,tempData,msg);
	    }
  }
  
  private void sendFinalMessage(Server serv,ClientWorker worker,Object [] tempData,String msg){
        try {
        	HttpSession session = worker.getSession();
            String id = session.getId();
            String userId = (String) session.getAttribute("userid");
            JSONObject balData = MessageGate.fetchSMSBalance(userId); //data about sms balance
            int balance = balData.optInt("sms_balance");
            String accType = balData.optString("sms_account_type");
            int sentCount = 0; //how many messages were actually sent
            JSONObject requestData = worker.getRequestData();
            JSONArray listIds = requestData.optJSONArray("list_ids");
            JSONArray phoneNos = requestData.optJSONArray("phones"); //edited phone nos
            JSONArray names = requestData.optJSONArray("names"); //edited names
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            for(int x=0; x<listIds.length(); x++){
               Iterable<Entity> entities = Datastore.getMultipleEntities("SMS_LISTS", "LIST_ID",listIds.optString(x),FilterOperator.EQUAL);
               JSONObject listData= Datastore.entityToJSON(entities);
               JSONArray listPhones =listData.optJSONArray("PERSON_PHONE");
               JSONArray listNames =listData.optJSONArray("PERSON_NAME");
               //get the phone numbers from lists and append to the phone numbers from edits
               phoneNos.toList().addAll(listPhones.toList());
               names.toList().addAll(listNames.toList());
            }
            for(int y=0; y<phoneNos.length(); y++){
               if(sentCount >= balance && accType.equals("prepaid")){
                   //we should stop you from sending messages if sms balance is 0 and sms account is prepaid
                   //we should allow you to send if your account is postpaid
                   //charge account and exit
                   MessageGate.noteSMSUsage(userId,sentCount);
                   worker.setResponseData(Message.FAIL);
                   serv.messageToClient(worker);
                   return;
                }
               else if(stopSendingMessages.get(id)){
                 MessageGate.noteSMSUsage(userId,sentCount);
                 worker.setResponseData(Message.SUCCESS);
                 serv.messageToClient(worker);
                 return;
               }
               String phoneNo = phoneNos.optString(y);
               String name = names.optString(y);
               int count = commitMessage(phoneNo,msg,id,userId,name);
               sentCount = sentCount+count;
              //send the list messages
            }
           UserAction action = new UserAction(serv,worker,"SEND MESSAGE TO LIST");
           action.saveAction();
        } catch (Exception ex) {
        	worker.setResponseData(Message.FAIL);
            serv.messageToClient(worker);
        }
      
  }
  
  private void messageWasSent(String id){
    Integer val = messageSent.get(id);
    val++;
    messageSent.put(id, val);
  }
  
    private void messageWasNotSent(String id,String msg,String rcpPhone,String rcpName){
        failedMessages.get(id).add(msg);
        failedRecipients.get(id).add(rcpPhone);
        failedRecipientNames.get(id).add(rcpName);
    }
  
  @Endpoint(name="resend_failed_messages")
  public void resendFailedMessages(Server serv, ClientWorker worker){
        try {
            String userId=(String) worker.getSession().getAttribute("userid");
            UserAction action=new UserAction(serv, worker, "RESEND FAILED MESSAGES");
            int sentMessageCount=0;
            JSONObject balData = MessageGate.fetchSMSBalance(userId);
            int balance=balData.optInt("sms_balance");
            String accType=balData.optString("sms_account_type");
            String sessionId=worker.getSession().getId();
            ArrayList<String> failedRcp=failedRecipients.get(sessionId);
            if(failedRcp==null){
               return;
            }
            ArrayList<String> failedName = failedRecipientNames.get(sessionId);
            ArrayList<String> failedMsg = failedMessages.get(sessionId);
            ArrayList<String> newFailedRcp = new ArrayList();
            ArrayList<String> newFailedMsg = new ArrayList();
            ArrayList<String> newFailedName = new ArrayList();
            newFailedRcp.addAll(failedRcp);
            newFailedMsg.addAll(failedMsg);
            newFailedName.addAll(failedName);
            failedRecipients.put(sessionId,new ArrayList<String>());
            failedMessages.put(sessionId, new ArrayList<String>());
            failedRecipientNames.put(sessionId, new ArrayList<String>());
            messageSent.put(sessionId,0);
            for(int x=0; x<failedRcp.size(); x++){
               if(stopSendingMessages.get(sessionId)){ 
                  MessageGate.noteSMSUsage(userId,sentMessageCount);
                  return; 
               }
               if(sentMessageCount>=balance && accType.equals("prepaid")){
                  //you have sent enough messages charge and exit
                   System.out.println("insufficient balance in sms_account");
                   MessageGate.noteSMSUsage(userId,sentMessageCount);
                   return;
                }
               String rcp=newFailedRcp.get(x);
               String msg=newFailedMsg.get(x);
               String name=newFailedName.get(x);
               int sentCount=commitMessage(rcp, msg, sessionId, userId,name);
               sentMessageCount=sentMessageCount+sentCount;
            }
            worker.setResponseData(Message.SUCCESS);
            serv.messageToClient(worker);
            action.saveAction();
            failedRecipients.put(sessionId,new ArrayList<String>());
            failedMessages.put(sessionId,new ArrayList<String>());
        } catch (Exception ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  

  
  @Endpoint(name="sms_data")
  public void smsData(Server serv, ClientWorker worker){
	  String userId=(String) worker.getSession().getAttribute("userid");
      JSONObject data = MessageGate.fetchSMSBalance(userId);
      worker.setResponseData(data);
      serv.messageToClient(worker);
  }
  

     //we need to deduct the amount of messages this school has
     //as per their usage, the fast step is to get their sms account
     //no and then use it to charge it, since we are using google cloud nosql
     //we can only charge one school connection at a time
     //we need to charge in bulk i.e deduct after all messages have been sent
     //if we have sent a number of messages and the system fails, store this
     //number of messages locally and the first thing we do when somebody tries
     //to send a new message is to charge their account
     //so we will have a table called SMS_DATA with pending charges and the initiators name
     //when we start we charge all the pending charges and then try to send again  
 
  
  @Endpoint(name="stop_sending_messages")
  public void stopMessagePropagation(Server serv, ClientWorker worker){
	   String sessionId = worker.getSession().getId();
       stopSendingMessages.put(sessionId,true);
       worker.setResponseData(Message.SUCCESS);
       serv.messageToClient(worker);
  }
  
  
  @Endpoint(name="check_message_progress")
  public void checkMessageProgress(Server serv, ClientWorker worker){
	  try {
            String id=worker.getSession().getId();
            JSONObject data=new JSONObject();
            Integer val = messageSent.get(id);
            ArrayList<String> failed = failedMessages.get(id);
            int size;
            if(val==null){
             val=0;
            }
            if(failed==null){
               size=0; 
            }
            else{
              size=failed.size();
            }
            JSONArray rcpNames=new JSONArray(failedRecipientNames.get(id));
            JSONArray rcpPhones=new JSONArray(failedRecipients.get(id));
            data.put("message_sent", val);
            data.put("message_fail", size);
            data.put("failed_rcp_names", rcpNames);
            data.put("failed_rcp_phones", rcpPhones);
            worker.setResponseData(data);
            serv.messageToClient(worker);
        } catch (JSONException ex) {
            Logger.getLogger(InternetMessageService.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
  @Endpoint(name="fetch_outbox")
  public void fetchOutbox(Server serv, ClientWorker worker){
	  JSONObject obj = worker.getRequestData();
	  serv.log(obj,Level.SEVERE,this.getClass());
	  int start = obj.optInt("start");
	  int stop = obj.optInt("stop");
	  String userId = worker.getSession().getAttribute("userid").toString();
	  FilterPredicate filter = new FilterPredicate("USER_ID",FilterOperator.EQUAL,userId);
	  Iterable<Entity> entities = Datastore.getMultipleEntities("MESSAGE_OUTBOX", "TIMESTAMP",SortDirection.DESCENDING,filter);
	  int count = 0;
	  JSONArray outbox = new JSONArray();
	  for(Entity en : entities){
		 if(count < start){
			 //do nothing
		 }
		 else if(count > stop){
			break;
		 }
		 else{
			outbox.put(Datastore.entityToJSON(en)); 
		 }
		 count++;
	  }
	  worker.setResponseData(outbox);
      serv.messageToClient(worker);
  }
  
  
  private static String correctPhoneNo(String phoneNo){
     if(phoneNo.startsWith("0")){
        phoneNo="+254"+phoneNo.substring(1);
     }
     else if(phoneNo.startsWith("254")){
        phoneNo="+"+phoneNo;
     }
     else{
        phoneNo="+254"+phoneNo; 
     }
     return phoneNo;
  }
  
  private int commitMessage(String phoneNo,String msg,String sessionId,String userId,String rcpName){
	  int sentCount = ((int)Math.ceil(msg.length()/160.0) );
	  phoneNo = correctPhoneNo(phoneNo);
	  Server.log("Request send : "+phoneNo,Level.SEVERE,InternetMessageService.class);
	  boolean sent = MessageGate.sendMessage(userId, phoneNo, msg);
	  if (sent) {
		  messageWasSent(sessionId); 
		  saveToOutbox(userId,phoneNo,msg);
	  }
	  else{
		  messageWasNotSent(sessionId,msg,phoneNo,rcpName);  
      }
      return sentCount;
   }
  
  private void saveToOutbox(String userId,String phoneNo,String msg){
	  Entity en = new Entity("MESSAGE_OUTBOX");
	  en.setProperty("USER_ID",userId);
	  en.setProperty("PHONE_NO",phoneNo);
	  en.setProperty("MESSAGE",new Text(msg));
	  en.setProperty("TIMESTAMP",System.currentTimeMillis());
	  Datastore.insert(en);
  }
  
  
  
  

  
  private Object[] parseMessage(String msg,Server serv,ClientWorker worker){
       // we are going to send custom messages to each phone number
      StringTokenizer token=new StringTokenizer(msg, "$"); 
      String [] msgTemplate =new String[token.countTokens()];
      HashMap locations=new HashMap();
      int count=0;
      while(token.hasMoreTokens()){
        String tk=token.nextToken();
        if(tk.startsWith("{")){
           try {
               //this is a json object template, so parse it
               JSONObject template =new JSONObject(tk);
               //check the data key
               JSONObject msgData= template.optJSONObject("data");
               if(msgData==null ){
                    //this information is insufficient we need a data key
                 worker.setReason("The data key is missing");
                 worker.setResponseData(Message.FAIL);
                 serv.messageToClient(worker);
                 return null;
               }
               else{
                  String table = msgData.optString("table");
                  String col = msgData.optString("column");
                  if(table.isEmpty() && col.isEmpty()){
                     worker.setReason("No table or column specified for data key");
                     worker.setResponseData(Message.FAIL);
                     serv.messageToClient(worker);
                     return null;  
                  }
                  else{
                    locations.put(count,template);
                  }
               }
           } catch (Exception ex) {
               //this means this is an invalid json string,tell the client that.
        	   worker.setResponseData(new Exception("The template "+tk+" is invalid"));
               serv.exceptionToClient(worker);
               return null;
           }
       }
       else{ 
         msgTemplate[count] = tk;
       }
      count++;
     }
   return new Object [] {msgTemplate,locations};
}
  private String resolveTemplates(String table,String template,JSONObject data,int count){
      if(template.equals("exam")){
         return "";
      }
      else if(template.equals("exam2")){
        return "";
      }
      return "";
  }
  
}
